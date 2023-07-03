prefix    = "mil"
env_short = "p"
env       = "prod"

github_repository_environment_cd = {
  protected_branches     = true
  custom_branch_policies = false
  reviewers_teams = [
    "infrastructure-admins",
    "swc-mil-team",
  ]
}
