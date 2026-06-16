# Where's XYZ 📍 - Lokalizacja w czasie rzeczywistym i zarządzanie grupami

**Where's XYZ** to kompleksowa aplikacja na system Android, stworzona dla osób, które chcą pozostać w stałym kontakcie ze swoimi bliskimi lub grupą znajomych. Niezależnie od tego, czy podróżujesz w grupie, spotykasz się ze znajomymi w zatłoczonym mieście, czy organizujesz rodzinną wycieczkę, ta aplikacja dostarcza narzędzi, dzięki którym nikt się nie zgubi.

---

## 🚀 Kluczowe Funkcje w Szczegółach

### 🔐 Zaawansowane Bezpieczeństwo i Autoryzacja
*   **Firebase Authentication**: Solidny system rejestracji i logowania oparty na e-mailu i haśle.
*   **Integracja Biometryczna**: Szybki i bezpieczny dostęp do aplikacji za pomocą odcisku palca lub rozpoznawania twarzy (Android BiometricPrompt API).
*   **Szyfrowane Przechowywanie**: Wrażliwe dane użytkownika są obsługiwane za pomocą `EncryptedSharedPreferences`, co gwarantuje bezpieczeństwo lokalne.

### 👥 Dynamiczna Koordynacja Grupowa
*   **Unikalne Kody Grup**: Grupy są identyfikowane przez łatwe do udostępnienia 4-cyfrowe kody, co upraszcza proces dołączania.
*   **Relacje Wiele-do-Wielu (M:N)**: Elastyczny system, w którym użytkownicy mogą być członkami wielu kręgów jednocześnie (np. "Rodzina", "Znajomi z pracy", "Ekipa na wakacje").
*   **Synchronizacja w Czasie Rzeczywistym**: Dzięki Firestore każda zmiana w grupie (nowy członek, zmiana nazwy) jest natychmiast widoczna na wszystkich urządzeniach.

### 🗺️ Mapy Live i Interakcja
*   **Integracja z Google Maps**: Precyzyjne śledzenie lokalizacji członków grupy na mapie przy użyciu Google Maps SDK.
*   **Interaktywne Pingi (1:N)**: Unikalny system komunikacji pozwalający wysyłać szybkie powiadomienia emoji (Pingi), aby przyciągnąć uwagę bez konieczności pisania.
*   **Zarządzanie Wydarzeniami (1:1)**: Każda grupa może posiadać przypisane wydarzenie z opisem, zdjęciem i ramami czasowymi, widoczne dla wszystkich członków.

---

## 🛠 Architektura Techniczna

Projekt opiera się na zasadach **Clean Architecture** oraz wzorcu projektowym **MVVM (Model-View-ViewModel)**, co zapewnia skalowalność i łatwość testowania.

*   **Jetpack Compose**: W 100% deklaratywny interfejs użytkownika zbudowany z nowoczesnych komponentów Material 3.
*   **Hilt (Dagger)**: Standaryzowane wstrzykiwanie zależności dla modułowego kodu.
*   **Kotlin Coroutines & Flow**: Programowanie asynchroniczne zapewniające płynność działania UI i strumieni danych czasu rzeczywistego.
*   **Firebase Suite**:
    *   **Cloud Firestore**: NoSQL-owa baza danych czasu rzeczywistego.
    *   **Firebase Auth**: Zarządzanie użytkownikami.
    *   **Cloud Messaging**: Powiadomienia push.
*   **Retrofit & Gson**: Gotowy stos sieciowy do przyszłych integracji z zewnętrznymi API REST.

---

## 📊 Schemat Bazy Danych (Firestore)

| Kolekcja | Opis | Relacje |
| :--- | :--- | :--- |
| `users` | Profile użytkowników, ustawienia i listy grup. | `groups` (Wiele-do-Wielu) |
| `groups` | Metadane grup, kody i identyfikatory członków. | `users` (M:M), `events` (1:1) |
| `events` | Szczegóły spotkań specyficznych dla danej grupy. | `groups` (1:1) |
| `pings` | Interaktywne alerty wysyłane przez użytkowników. | `users` (1:N) |

---

## ⚙️ Konfiguracja i Instalacja

### Wymagania wstępne
*   Android Studio Ladybug lub nowsze.
*   JDK 17+.
*   Projekt w konsoli Firebase.

### Kroki instalacji
1.  **Sklonuj repozytorium**: `git clone https://github.com/twoj-uzytkownik/wheresxyz.git`
2.  **Konfiguracja Firebase**: 
    *   W Firebase Console stwórz aplikację Android o pakiecie `com.example.wheresxyz`.
    *   Pobierz plik `google-services.json` i umieść go w katalogu `app/`.
3.  **Klucze API**:
    *   Otwórz plik `local.properties` w głównym folderze projektu.
    *   Dodaj linię: `MAPS_API_KEY=twoj_klucz_google_maps`.
4.  **Build**: Zsynchronizuj Gradle i uruchom projekt (Run).

---

## 🛡️ Licencja
Projekt stworzony w celach edukacyjnych jako część projektu na 6 semestr studiów.
