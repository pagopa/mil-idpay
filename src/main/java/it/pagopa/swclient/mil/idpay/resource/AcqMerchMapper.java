/**
 * THIS IS TEMPORARY AND IT WILL BE ADDRESSED WITH THE NEW VERSION OF AUTH MECHANISM.
 */
package it.pagopa.swclient.mil.idpay.resource;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.logging.Log;
import it.pagopa.swclient.mil.bean.CommonHeader;

/**
 * 
 */
public class AcqMerchMapper {
	private static final Map<String, String> ACQ_MAP = new HashMap<>();
	static {
		ACQ_MAP.put("4585625", "PAGOPA");
		ACQ_MAP.put("4585626", "PAGOPA");
	}
	
	private static final Map<String, String> MERCH_MAP = new HashMap<>();
	static {
		MERCH_MAP.put("28405fHfk73x88D", "33444433488"); // RNZPMP80A44X000M
		MERCH_MAP.put("12346789", "33444433488"); // RNZPMP80A44X000M
	}
	
	private AcqMerchMapper() {
	}
	
	public static void map(CommonHeader header) {
		String acq = ACQ_MAP.get(header.getAcquirerId());
		if (acq != null) {
			Log.debugf("AcquirerId remapped to %s.", acq);
			header.setAcquirerId(acq);
		}
		
		String merch = MERCH_MAP.get(header.getMerchantId());
		if (merch != null) {
			Log.debugf("MerchantId remapped to %s.", merch);
			header.setMerchantId(merch);
		}
	}
}