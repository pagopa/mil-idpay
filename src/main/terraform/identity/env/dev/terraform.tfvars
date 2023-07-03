prefix    = "mil"
env_short = "d"
env       = "dev"

github_repository_environment_cd = {
  protected_branches     = false
  custom_branch_policies = true
  reviewers_teams = [
    "infrastructure-admins",
    "swc-mil-team",
  ]
}
