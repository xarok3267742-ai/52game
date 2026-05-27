# Release Signing Guide

`Цветной Квартал` поддерживает безопасную локальную подпись release bundle через `keystore.properties`. Реальный файл `keystore.properties` и binary upload keystore files не должны попадать в репозиторий.

Официальная модель Google Play: app bundle нужно подписывать upload key перед загрузкой в Play Console, а Play App Signing подписывает APK для устройств app signing key. Источники: https://developer.android.com/guide/publishing/app-signing.html и https://support.google.com/googleplay/android-developer/answer/9842756?hl=en

## 1. Создать upload keystore

```bash
keytool -genkeypair -v \
  -keystore "$HOME/.android/color-quarter-upload.jks" \
  -alias color-quarter-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Пароли сохранить в менеджере паролей. Этот файл нельзя коммитить, отправлять в чат или хранить рядом с публичным исходным кодом.
Храните binary upload keystore вне project tree, например в `$HOME/.android/`.

## 2. Создать keystore.properties

Скопировать пример:

```bash
cp keystore.properties.example keystore.properties
```

Заполнить:

```properties
storeFile=/absolute/path/outside/repo/color-quarter-upload.jks
storePassword=<store-password>
keyAlias=color-quarter-upload
keyPassword=<key-password>
```

`keystore.properties` добавлен в `.gitignore`.
`scripts/validate_release_candidate.sh` дополнительно проверяет, что `.gitignore` закрывает `keystore.properties`, `*.jks`, `*.keystore`, `*.p12`, `*.pfx` и `local.properties`, а binary keystore files не лежат внутри project tree.

## 3. Собрать signed AAB

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

./gradlew clean :app:bundleRelease
```

Результат:

```text
app/build/outputs/bundle/release/app-release.aab
```

Если `keystore.properties` отсутствует, Gradle всё равно соберёт release bundle, но он останется неподписанным production upload key.

## 4. Проверить подпись

```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
./gradlew :app:signingReport
```

В Play Console после первого upload проверить SHA-1/SHA-256 upload certificate в `Test and release > Setup > App signing`.

## 5. Что делать вручную в Play Console

- Создать приложение.
- Включить Play App Signing.
- Выбрать upload key flow.
- Загрузить signed `.aab`.
- Сохранить certificate fingerprints в release notes/internal docs.

## Риски

- Потеря upload key блокирует загрузку новых версий, пока не будет выполнен reset upload key через Play Console.
- Нельзя менять `applicationId` после публикации.
- Нельзя публиковать debug build или build с `applicationIdSuffix=.debug`.
