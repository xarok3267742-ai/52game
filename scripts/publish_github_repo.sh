#!/usr/bin/env bash
set -euo pipefail

REPO_OWNER="${REPO_OWNER:-xarok3267742-ai}"
REPO_NAME="${REPO_NAME:-color-quarter-android}"
REPO_FULL_NAME="$REPO_OWNER/$REPO_NAME"
REMOTE_URL="https://github.com/$REPO_FULL_NAME.git"

cd "$(dirname "$0")/.."

if git ls-files | grep -E '(^|/)(keystore\.properties|.*\.(jks|keystore|p12|pfx)|local\.properties)$' >/dev/null; then
  echo "Refusing to publish: private signing/local files are tracked." >&2
  exit 1
fi

git remote get-url origin >/dev/null 2>&1 || git remote add origin "$REMOTE_URL"
git remote set-url origin "$REMOTE_URL"

if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
  if gh repo view "$REPO_FULL_NAME" >/dev/null 2>&1; then
    git push -u origin main
  else
    gh repo create "$REPO_FULL_NAME" --private --source=. --remote=origin --push
  fi
  exit 0
fi

if [ -z "${GH_TOKEN:-}" ]; then
  echo "GitHub auth is unavailable. Run 'gh auth login' or set GH_TOKEN." >&2
  exit 1
fi

api_status="$(
  curl -sS -o /tmp/color-quarter-github-repo.json -w "%{http_code}" \
    -H "Authorization: Bearer $GH_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    "https://api.github.com/repos/$REPO_FULL_NAME"
)"

if [ "$api_status" = "404" ]; then
  create_status="$(
    curl -sS -o /tmp/color-quarter-github-create.json -w "%{http_code}" \
      -X POST \
      -H "Authorization: Bearer $GH_TOKEN" \
      -H "Accept: application/vnd.github+json" \
      https://api.github.com/user/repos \
      -d "{\"name\":\"$REPO_NAME\",\"private\":true,\"description\":\"Цветной Квартал Android release candidate\",\"has_issues\":true,\"has_projects\":false,\"has_wiki\":false}"
  )"
  if [ "$create_status" != "201" ]; then
    echo "Failed to create GitHub repository. HTTP $create_status" >&2
    cat /tmp/color-quarter-github-create.json >&2
    exit 1
  fi
elif [ "$api_status" != "200" ]; then
  echo "Failed to inspect GitHub repository. HTTP $api_status" >&2
  cat /tmp/color-quarter-github-repo.json >&2
  exit 1
fi

git push -u origin main
