@echo off
echo Kompilacja dokumentacji LaTeX...
echo.

REM Sprawdz czy pdflatex jest dostepny
where pdflatex >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo BLAD: pdflatex nie zostal znaleziony!
    echo Zainstaluj MiKTeX lub TeX Live.
    echo.
    echo Pobierz MiKTeX: https://miktex.org/download
    echo Lub TeX Live: https://www.tug.org/texlive/
    pause
    exit /b 1
)

echo [1/3] Pierwsza kompilacja dokumentacji...
pdflatex -interaction=nonstopmode dokumentacja.tex

echo.
echo [2/3] Druga kompilacja (dla spisu tresci)...
pdflatex -interaction=nonstopmode dokumentacja.tex

echo.
echo [3/3] Kompilacja diagramu klas...
pdflatex -interaction=nonstopmode class-diagram.tex

echo.
echo =============================================
echo Kompilacja zakonczona!
echo Pliki PDF:
echo   - dokumentacja.pdf
echo   - class-diagram.pdf
echo =============================================
echo.

REM Czyszczenie plikow tymczasowych
echo Czyszczenie plikow tymczasowych...
del /q *.aux *.log *.out *.toc *.lof *.lot 2>nul

echo Gotowe!
pause
