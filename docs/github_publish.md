# GitHub Publish Notes

Дата: 27 мая 2026 г.

## Target Repository

```text
https://github.com/xarok3267742-ai/color-quarter-android
```

## Current Local State

- Branch: `main`
- Latest commit: `529e8e1 Release candidate 1.0.0`
- Remote: `origin https://github.com/xarok3267742-ai/color-quarter-android.git`
- Push status: blocked locally because GitHub authentication is not available in this terminal session.

## Security Rules

Never publish:

- `keystore.properties`
- `*.jks`
- `*.keystore`
- `*.p12`
- `*.pfx`
- `local.properties`
- Desktop folder `PRIVATE_SIGNING_DO_NOT_UPLOAD`

Safe tracked signing file:

- `keystore.properties.example`

## Publish With GitHub CLI

```bash
cd <project-root>
gh auth login
gh repo create xarok3267742-ai/color-quarter-android --private --source=. --remote=origin --push
```

If the repository already exists:

```bash
cd <project-root>
git push -u origin main
```

## Publish With Token

Set a GitHub token with repo creation/push permission:

```bash
export GH_TOKEN="..."
scripts/publish_github_repo.sh
```

The script creates the repository through GitHub API if it does not exist, then pushes `main`.

## Handoff Files

Desktop handoff:

```text
<desktop-handoff>/Цветной Квартал - Google Play RC 1.0.0
```

Safe public archive:

```text
<desktop-handoff>/Цветной Квартал - Google Play RC 1.0.0/ColorQuarter-1.0.0-PUBLIC_UPLOAD_SAFE.zip
```
