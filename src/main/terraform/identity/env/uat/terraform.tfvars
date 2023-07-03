prefix    = "mil"
env_short = "u"
env       = "uat"

github_repository_environment_cd = {
  protected_branches     = false
  custom_branch_policies = true
  reviewers_teams = [
    "infrastructure-admins",
    "swc-mil-team",
  ]
}
