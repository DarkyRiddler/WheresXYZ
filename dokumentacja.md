# Dokumentacja Projektu - Where's XYZ 📍

## 1. Wstęp i Cel Projektu
**Where's XYZ** to zaawansowana aplikacja mobilna stworzona z myślą o bezpieczeństwie i łatwej koordynacji grupowej. Głównym celem aplikacji jest rozwiązanie problemu "gubienia się" członków grupy podczas wspólnych wyjazdów, festiwali czy wycieczek w nieznane miejsca. Aplikacja łączy w sobie precyzyjną geolokalizację z szybką, intuicyjną komunikacją.

## 2. Architektura Systemu
Projekt został zbudowany zgodnie z najnowszymi standardami programowania na platformę Android:
*   **Wzorzec Projektowy**: MVVM (Model-View-ViewModel), który oddziela logikę biznesową od warstwy prezentacji.
*   **Clean Architecture**: Wykorzystanie repozytoriów jako warstwy abstrakcji dla danych, co pozwoliło na łatwą migrację z systemów Mock/Realtime Database na Cloud Firestore.
*   **DI (Dependency Injection)**: Dagger Hilt zapewnia modułowość i ułatwia testowanie poszczególnych komponentów.
*   **UI**: Jetpack Compose – w pełni deklaratywny interfejs użytkownika, oferujący płynne animacje i nowoczesny wygląd Material 3.

## 3. Szczegółowy Opis Funkcjonalności

### A. Zarządzanie Użytkownikiem i Bezpieczeństwo
*   **Autoryzacja**: Integracja z Firebase Auth umożliwia bezpieczne przechowywanie kont.
*   **Biometria**: Wykorzystanie czytnika linii papilarnych lub skanu twarzy pozwala na szybki dostęp do mapy bez konieczności wpisywania hasła przy każdym uruchomieniu.
*   **Profil Użytkownika**: Każdy użytkownik posiada unikalny `user_code` (4-cyfrowy), ułatwiający identyfikację wewnątrz systemu.

### B. Grupy i Relacje (Wiele-do-Wielu)
Aplikacja wspiera złożoną strukturę społeczną:
*   Użytkownik może należeć do wielu grup jednocześnie (np. "Rodzina", "Wycieczka 2024").
*   Każda grupa generuje unikalny kod dostępu.
*   Dane są zsynchronizowane w taki sposób, że zmiana pozycji jednego członka jest natychmiast widoczna u wszystkich pozostałych dzięki mechanizmom `SnapshotListener` w Firestore.

### C. Wydarzenia (Relacja 1:1)
Grupa może zdefiniować jedno aktywne wydarzenie (np. "Koncert na Scenie Głównej"), które zawiera:
*   Dokładne ramy czasowe (start/koniec).
*   Opis i zdjęcie miejsca spotkania.
*   Lokalizację docelową widoczną na mapie dla wszystkich członków grupy.

### D. System Pingów (Relacja 1:N)
Użytkownik może zdefiniować listę szybkich sygnałów (Pingów).
*   Pozwala to na wysłanie np. emoji "SOS" lub "Czekam tutaj" jednym kliknięciem.
*   Pingi są przypisane do konkretnego użytkownika, co pozwala na personalizację skrótów komunikacyjnych.

## 4. Model Danych (Firestore)

| Kolekcja | Kluczowe Pola | Opis Relacji |
| :--- | :--- | :--- |
| **users** | `id`, `user_code`, `email`, `groups[]` | Przechowuje listę ID grup, do których należy użytkownik. |
| **groups** | `id`, `group_code`, `members[]`, `event_id` | Przechowuje tablicę UID członków oraz opcjonalny link do wydarzenia. |
| **events** | `id`, `title`, `date_start`, `group_id` | Powiązane bezpośrednio z jedną konkretną grupą. |
| **pings** | `id`, `emoji`, `text`, `user_id` | Każdy rekord wskazuje na właściciela (użytkownika). |

## 5. Technologie i Biblioteki
*   **Kotlin & Coroutines**: Obsługa operacji asynchronicznych (pobieranie danych z sieci/bazy).
*   **Google Maps SDK**: Renderowanie mapy, markerów użytkowników i geofencing.
*   **Firebase Firestore**: Baza danych NoSQL czasu rzeczywistego.
*   **Hilt**: Zarządzanie cyklem życia zależności.
*   **Retrofit**: Gotowość do komunikacji z zewnętrznymi API REST.

## 6. Instrukcja Instalacji
1.  Pobierz repozytorium na dysk.
2.  W konsoli Firebase stwórz nowy projekt i dodaj aplikację Android (package: `com.example.wheresxyz`).
3.  Pobierz plik `google-services.json` i umieść go w folderze `app/`.
4.  W pliku `local.properties` dodaj klucz Google Maps: `MAPS_API_KEY=TWÓJ_KLUCZ`.
5.  Skompiluj i uruchom projekt w Android Studio.
