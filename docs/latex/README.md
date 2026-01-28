# Dokumentacja LaTeX - System Zarządzania Projektami

## Struktura plików

```
docs/latex/
├── dokumentacja.tex     # Główny dokument (8 rozdziałów)
├── class-diagram.tex    # Diagram klas UML w TikZ
├── compile.bat          # Skrypt kompilacji dla Windows
└── README.md            # Ten plik
```

## Zawartość dokumentacji

Dokument `dokumentacja.tex` zawiera 8 rozdziałów zgodnych z wytycznymi:

1. **Opis słowny realizowanego zadania**
   - Cel projektu
   - Zakres funkcjonalności
   - Opis problemu biznesowego
   - Stos technologiczny

2. **Opis formalny systemu**
   - Diagram przypadków użycia (TikZ)
   - Diagram ERD
   - Definicje tabel i typy danych
   - Główne kwerendy JPQL i SQL
   - Lista endpointów API
   - Przykładowe DTO (formularze)

3. **Normalizacja i więzy tabel**
   - Analiza 1NF, 2NF, 3NF, BCNF
   - Więzy PRIMARY KEY, FOREIGN KEY
   - Więzy UNIQUE i CHECK
   - Więzy NOT NULL
   - Akcje referencyjne

4. **Kontrola poprawności danych**
   - Bean Validation (JSR-380)
   - Walidacja w kontrolerach (@Valid)
   - Globalna obsługa wyjątków
   - Wartości domyślne
   - Walidacja plików
   - Maski wprowadzania (regex)

5. **Automatyczne tworzenie tabel**
   - Konfiguracja Hibernate DDL
   - Tryby ddl-auto
   - Definicje encji JPA z indeksami
   - Inicjalizacja danych testowych

6. **Rejestracja daty i czasu operacji**
   - Automatyczne timestampy (@CreationTimestamp)
   - System audytu
   - Integracja audytu z serwisami
   - Możliwość ręcznej korekty

7. **Instrukcja obsługi**
   - Wymagania systemowe
   - Instalacja
   - Konfiguracja
   - Uruchomienie (Docker, lokalne)
   - Dokumentacja API
   - Przykłady curl
   - Uruchomienie testów

8. **Kod źródłowy**
   - Struktura projektu
   - Główna klasa aplikacji
   - Konfiguracja bezpieczeństwa
   - Kontroler autentykacji

9. **Prezentacja projektu**
   - Scenariusze testowe
   - Demonstracja funkcjonalności
   - Wykorzystane narzędzia
   - Podsumowanie

## Wymagania

### Instalacja LaTeX

#### Windows (MiKTeX)
1. Pobierz MiKTeX: https://miktex.org/download
2. Zainstaluj z opcją "Install missing packages on-the-fly"
3. Uruchom `compile.bat`

#### Windows (TeX Live)
1. Pobierz TeX Live: https://www.tug.org/texlive/
2. Zainstaluj pełną dystrybucję
3. Uruchom `compile.bat`

### Wymagane pakiety LaTeX
- inputenc, fontenc, babel (polski)
- geometry, graphicx
- listings, xcolor (kod źródłowy)
- hyperref (linki)
- booktabs, longtable, tabularx (tabele)
- float, caption, subcaption
- fancyhdr, titlesec, tocloft
- enumitem, amsmath
- tikz + biblioteki (diagramy)

## Kompilacja

### Windows
```batch
cd docs\latex
compile.bat
```

### Linux/macOS
```bash
cd docs/latex
pdflatex dokumentacja.tex
pdflatex dokumentacja.tex  # drugi raz dla spisu treści
pdflatex class-diagram.tex
```

### Overleaf (online)
1. Utwórz nowy projekt na https://overleaf.com
2. Wgraj pliki `.tex`
3. Kompilacja automatyczna

## Wynik kompilacji

Po kompilacji zostaną wygenerowane:
- `dokumentacja.pdf` - główna dokumentacja (~30 stron)
- `class-diagram.pdf` - diagram klas UML (landscape A4)

## Personalizacja

Przed oddaniem projektu uzupełnij na stronie tytułowej:
- [Imię i Nazwisko]
- [Numer albumu]
- [Numer semestru]

```latex
\begin{tabular}{rl}
    \textbf{Autor:} & Jan Kowalski \\
    \textbf{Nr albumu:} & 123456 \\
    \textbf{Kierunek:} & Informatyka \\
    \textbf{Semestr:} & 4 \\
\end{tabular}
```

## Dodawanie zrzutów ekranu

Aby dodać zrzuty ekranu:

1. Zapisz obrazy w folderze `docs/latex/images/`
2. W dokumencie użyj:

```latex
\begin{figure}[H]
\centering
\includegraphics[width=0.8\textwidth]{images/screenshot.png}
\caption{Opis zrzutu ekranu}
\end{figure}
```

## Uwagi

- Dokument jest w języku polskim (babel)
- Kodowanie UTF-8
- Styl kodu: kolorowanie składni dla Java i SQL
- Diagramy stworzone w TikZ (wektorowe)
