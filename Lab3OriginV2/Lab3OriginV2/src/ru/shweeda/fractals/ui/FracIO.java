// Указываем, что класс принадлежит пакету ru.smak.fractals.ui
// (если используешь другой пакет, как ru.smak.fractals.io — измени соответствующим образом)
package ru.shweeda.fractals.ui;

import java.io.*;                         // Для работы с файлами
import java.util.HashMap;                // Для хранения координат как ключ-значение
import java.util.Map;                    // Интерфейс Map используется как возвращаемый тип

/**
 * Класс FracIO предоставляет методы для:
 * - сохранения фрактала в текстовый файл формата .frac;
 * - загрузки координат области просмотра из этого файла.
 */
public class FracIO {

    /**
     * Метод для сохранения текущей области фрактала в файл формата .frac.
     * @param file   Файл, в который будет производиться сохранение.
     * @param xMin   Минимальное значение X (левая граница области).
     * @param xMax   Максимальное значение X (правая граница области).
     * @param yMin   Минимальное значение Y (нижняя граница области).
     * @param yMax   Максимальное значение Y (верхняя граница области).
     * @throws IOException Если при записи в файл произойдёт ошибка.
     */
    public static void saveFrac(File file, double xMin, double xMax, double yMin, double yMax) throws IOException {
        // PrintWriter записывает текст в файл
        try (PrintWriter out = new PrintWriter(file)) {
            // Сохраняем координаты как пары "ключ=значение", каждая с новой строки
            out.println("xMin=" + xMin);
            out.println("xMax=" + xMax);
            out.println("yMin=" + yMin);
            out.println("yMax=" + yMax);
        }
    }

    /**
     * Метод для загрузки координат области фрактала из .frac-файла.
     * @param file Файл, из которого загружаются координаты.
     * @return Map, где ключи — названия координат, значения — числовые значения double.
     * @throws IOException Если файл не найден или произошла ошибка при чтении.
     */
    public static Map<String, Double> loadFrac(File file) throws IOException {
        // Создаём карту для хранения координат
        Map<String, Double> coords = new HashMap<>();

        // BufferedReader позволяет читать файл построчно
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            // Читаем каждую строку файла
            while ((line = in.readLine()) != null) {
                // Разделяем строку по знаку "="
                String[] parts = line.split("=");
                // Если строка корректно содержит пару "ключ=значение"
                if (parts.length == 2) {
                    // Добавляем в карту ключ и числовое значение
                    coords.put(parts[0], Double.parseDouble(parts[1]));
                }
            }
        }

        // Возвращаем карту координат: xMin, xMax, yMin, yMax
        return coords;
    }
}
