package it.pagopa.swclient.mil.idpay.azurekeyvault.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import it.pagopa.swclient.mil.idpay.azurekeyvault.bean.KeyNameAndVersion;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
public class KidUtil {
    /*
     *
     */
    @ConfigProperty(name = "quarkus.rest-client.azure-key-vault-api.url")
    String vaultBaseUrl;

    /*
     *
     */
    private Pattern patternForAzureKidWithNameAndVersion;


    /**
     *
     */
    @PostConstruct
    void init() {
        String temp = (vaultBaseUrl + "/keys/").replace("//keys", "/keys");
        patternForAzureKidWithNameAndVersion = Pattern.compile("^" + Pattern.quote(temp) + "(?<name>\\w+)\\/(?<version>\\w+)$");
    }

    /**
     *
     * @param kid
     * @return
     */
    public KeyNameAndVersion getNameAndVersionFromAzureKid(String kid) {
        KeyNameAndVersion nameAndVersion = new KeyNameAndVersion();
        if (kid != null) {
            Matcher m = patternForAzureKidWithNameAndVersion.matcher(kid);
            if (m.find()) {
                nameAndVersion.setName(m.group("name"));
                nameAndVersion.setVersion(m.group("version"));
            }
        }
        return nameAndVersion;
    }


    /**
     *
     * @param nameAndVersion
     * @return
     */
    public String getMyKidFromNameAndVersion(KeyNameAndVersion nameAndVersion) {
        return nameAndVersion.getName() + "/" + nameAndVersion.getVersion();
    }
}
