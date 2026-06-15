# Testy — Where's XYZ?

Przewodnik po uruchamianiu i rozszerzaniu testów w projekcie Android.

---

## Szybki start

```bash
# Unit testy (JVM) — bez emulatora
./gradlew testDebugUnitTest

# Testy UI — wymaga emulatora lub podłączonego urządzenia
./gradlew connectedDebugAndroidTest
```

W Android Studio: prawy klik na klasę testową → **Run**.

---

## Wymagania

### `google-services.json`

Plik w `app/google-services.json` (projekt **where-s-xyz**). Jest w `.gitignore`.

Checklista Firebase (Auth, Firestore, RTDB, Maps): **[FIREBASE_SETUP.md](../FIREBASE_SETUP.md)**

Bez tego pliku build padnie na tasku `:app:processDebugGoogleServices`.

### Środowisko

- JDK 11+
- Android SDK (compileSdk 35)
- Emulator — tylko dla `connectedDebugAndroidTest`

---

## Struktura testów

### Unit testy (`app/src/test/`)

| Plik | Co testuje |
| :--- | :--- |
| `util/AuthInputValidatorTest.kt` | Walidacja logowania, rejestracji, OAuth |
| `util/LocationMathTest.kt` | Odległość, bearing, formatowanie, staleness |
| `util/FirebaseKeySanitizerTest.kt` | Sanityzacja kluczy Firebase RTDB |
| `util/LocationParticipantMapperTest.kt` | Filtrowanie uczestników, sortowanie, przeliczanie dystansu |
| `data/model/EventTest.kt` | `Event.isActive` — granice czasowe |
| `data/model/SharedLocationTest.kt` | Staleness lokalizacji, `User.locationKey()` |
| `data/model/UserTest.kt` | `displayLabel()`, `locationKey()` |
| `ui/viewmodel/AuthViewModelTest.kt` | Splash, biometria, login, rejestracja, profil, logout, polskie błędy |
| `ui/viewmodel/GroupsViewModelTest.kt` | Ładowanie, tworzenie, join, zmiana nazwy, uprawnienia, opuszczenie grupy |
| `ui/viewmodel/EventsViewModelTest.kt` | Ładowanie, tworzenie, usuwanie wydarzeń |
| `data/model/GroupItemTest.kt` | Domyślne pola `GroupItem` i flaga `isMe` członka |
| `data/remote/ApiServiceIntegrationTest.kt` | Kontrakt REST (auth, users, groups) + błędy HTTP |

### Testy instrumentowane (`app/src/androidTest/`)

| Plik | Co testuje |
| :--- | :--- |
| `ui/screens/LoginScreenTest.kt` | Renderowanie, wpisywanie danych, wyświetlanie błędu |
| `ui/screens/RegisterScreenTest.kt` | Pola rejestracji, submit formularza, nawigacja do logowania |
| `ui/screens/GroupsTabTest.kt` | Lista grup, dialogi tworzenia/dołączania, szczegóły grupy |
| `ui/screens/ProfileTabTest.kt` | Profil użytkownika, edycja, wylogowanie |
| `ui/screens/EventsTabHeaderSectionTest.kt` | Pusty stan wydarzeń, banner udostępniania lokalizacji |
| `ExampleInstrumentedTest.kt` | Smoke test — poprawny `packageName` |

---

## Integracyjne testy API

`ApiServiceIntegrationTest` weryfikuje, że `ApiService` i modele w `data/remote/model/ApiModels.kt` zgadzają się z kontraktem opisanym w `backend_integration_guide.md`.

- Używa **OkHttp MockWebServer** — nie potrzeba działającego backendu.
- Sprawdza deserializację odpowiedzi (login, register, OAuth, groups, create/join), payload requestów i błędy HTTP (401, 404, 409, 500).

---

## Zależności testowe

W `app/build.gradle.kts`:

| Biblioteka | Zastosowanie |
| :--- | :--- |
| JUnit 4 | Framework testów |
| `kotlinx-coroutines-test` | ViewModele, `runTest`, `advanceUntilIdle` |
| MockK | Mocki repozytoriów i `TokenManager` |
| OkHttp MockWebServer | Testy `ApiService` |
| Turbine | Asynchroniczne Flow (do użycia przy rozszerzaniu) |
| Compose UI Test | Testy instrumentowane ekranów |

---

## Raporty

Po `./gradlew testDebugUnitTest`:

```
app/build/reports/tests/testDebugUnitTest/index.html
```

Po `./gradlew connectedDebugAndroidTest`:

```
app/build/reports/androidTests/connected/index.html
```

---

## Co dodać dalej

- **ViewModel:** `LocationSyncViewModel` — pełny test z Robolectric / fake `LocationShareService`
- **Repozytoria Firebase:** emulator Firebase albo interfejsy + fake'i
- **UI:** pełny `EventsTab` z listą wydarzeń (wymaga Hilt test lub dalszego refaktoru)
- **API:** nagłówek `Authorization: Bearer` w `getCurrentUser()` + test kontraktu JWT

### Ostatnio dodane

- `GroupItemTest`, rozszerzone `GroupsViewModelTest` i `AuthViewModelTest` (grupy + użytkownicy)
- API: `createGroup`, `joinGroup`, `loginWithOAuth` + błędy 409/404 w `ApiServiceIntegrationTest`
- UI: interakcje w `LoginScreenTest`, `RegisterScreenTest`, `GroupsTabTest`, nowy `ProfileTabTest`
