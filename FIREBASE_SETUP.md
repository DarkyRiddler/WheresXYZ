# Firebase — konfiguracja Where's XYZ

Projekt Firebase: **where-s-xyz** (`app/google-services.json`).

## 1. Włącz usługi w konsoli

Otwórz [Firebase Console → where-s-xyz](https://console.firebase.google.com/).

### Authentication
- **Sign-in method** → włącz **Email/Password**
- (Opcjonalnie) **Google** — wymaga SHA-1 debug keystore i ponownego pobrania `google-services.json`
- (Opcjonalnie) **Anonymous** — używane przez udostępnianie lokalizacji, gdy sesja anonimowa jest potrzebna

### Firestore Database
- **Create database** → wybierz region (np. `europe-west`)
- Na start możesz użyć **test mode**, potem wgraj reguły z repo (krok 2)

### Realtime Database
- **Create Database** → ten sam region co Firestore
- Wgraj reguły z `database.rules.json` (krok 2)

## 2. Wgraj reguły bezpieczeństwa

Z katalogu projektu (wymaga [Firebase CLI](https://firebase.google.com/docs/cli)):

```bash
npm install -g firebase-tools
firebase login
firebase use where-s-xyz
firebase deploy --only firestore:rules,database
```

Pliki w repo:
| Plik | Usługa |
|------|--------|
| [firestore.rules](firestore.rules) | Firestore — `Users`, `Groups`, `Events` |
| [database.rules.json](database.rules.json) | RTDB — `live_locations` |

Reguły Firestore (MVP): zalogowany użytkownik może czytać profile; pisać tylko w swój dokument `Users/{uid}`. Grupy i wydarzenia — read/write dla zalogowanych (można zaostrzyć później).

## 3. Maps API

W [Google Cloud Console](https://console.cloud.google.com/) (projekt powiązany z Firebase):
- Włącz **Maps SDK for Android**
- Klucz API jest w `local.properties` jako `MAPS_API_KEY` (skopiowany z `google-services.json`)

## 4. Pierwsze uruchomienie apki

1. `./gradlew installDebug` lub **Run** w Android Studio (emulator/telefon)
2. **Zarejestruj się** — email + hasło (min. 6 znaków)
3. Po zalogowaniu: zakładki **Profil**, **Grupy**, **Wydarzenia**

Gotowego konta testowego w repo **nie ma** — konto tworzysz w apce lub w Authentication → Users.

## 5. Google Sign-In (opcjonalnie)

1. Android Studio → Gradle → `:app` → `signingReport` → skopiuj **SHA-1** debug
2. Firebase → Project settings → Android app → **Add fingerprint**
3. Pobierz nowy `google-services.json` (powinien mieć niepusty `oauth_client`)
4. Zastąp `app/google-services.json` i przebuduj apkę

## 6. Typowe problemy

| Objaw | Przyczyna |
|-------|-----------|
| Błąd przy logowaniu / rejestracji | Email/Password wyłączone w Authentication |
| Puste grupy po utworzeniu | Firestore nie utworzony lub reguły blokują zapis |
| Mapa szara / błąd Maps | Brak `MAPS_API_KEY` w `local.properties` lub Maps SDK wyłączone |
| Lokalizacja nie idzie na mapę | RTDB nie utworzony lub reguły `live_locations` nie wgrane |
| Google login nie działa | Brak SHA-1 / pusty `oauth_client` w `google-services.json` |

## Kolekcje Firestore (używane w kodzie)

- `Users` — profil użytkownika (document ID = Firebase Auth UID)
- `Groups` — grupy i członkowie
- `Events` — wydarzenia powiązane z `groupId`

## Realtime Database

- `live_locations/{eventId}/{userKey}` — pozycje na żywo podczas wydarzenia
