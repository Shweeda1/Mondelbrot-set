// Пакет, к которому относится окно приложения
package ru.smak.fractals.ui;

// Импорты вспомогательных и графических компонентов
import ru.smak.Dialogs;
import ru.smak.imgfiles.FileData;
import ru.smak.imgfiles.NonImageFormatException;
import ru.smak.math.fractals.Mandelbrot;
import ru.smak.Converter;
import ru.smak.painting.CartesianPainter;
import ru.smak.painting.FractalPainter;
import ru.smak.fractals.ui.FracIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;


public class MainWindow extends JFrame {

    // Стандарты для компоновки элементов (минимальный и максимальный размеры)
    private static final int MIN_SZ = GroupLayout.PREFERRED_SIZE;
    private static final int MAX_SZ = GroupLayout.DEFAULT_SIZE;

    // Панель рисования и основные компоненты фрактала
    private PaintPanel mainPanel;
    private final Mandelbrot m = new Mandelbrot();               // сам фрактал
    private final Converter conv = new Converter((long) -2.0, (long)1.0, (long)-1.0, (long)1.0); // преобразование координат
    private final FractalPainter fp = new FractalPainter(conv, m);          // отрисовка фрактала
    private final CartesianPainter cp = new CartesianPainter(conv);         // отрисовка осей (если нужно)

    // Элементы меню
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu();                   // меню "Файл"
    private final JMenuItem save = new JMenuItem();               // пункт "Сохранить..."
    private final JMenuItem openFrac = new JMenuItem();           // пункт "Открыть .frac..."

    private Point firstPoint = null;
    private Point secondPoint = null;
    private final Stack<ViewState> viewHistory = new Stack<>();
    private final Stack<ViewState> redoStack = new Stack<>();
    /**
     * Конструктор окна приложения
     */
    public MainWindow() {
        Dimension minSz = new Dimension(800, 600);
        setMinimumSize(minSz);
        setTitle("Множество Мандельброта");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // завершать приложение при закрытии окна

        initializeComponents(); // создаём и настраиваем панель рисования

        // Настройка меню
        save.setText("Сохранить...");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        save.addActionListener(e -> saveFileUnified());

        openFrac.setText("Открыть .frac...");
        openFrac.addActionListener(e -> openFracFile());

        fileMenu.setText("Файл");
        fileMenu.add(save);
        fileMenu.add(openFrac);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Компоновка окна
        GroupLayout gl = new GroupLayout(getContentPane());
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, MAX_SZ, MAX_SZ, MAX_SZ)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(mainPanel, MAX_SZ, MAX_SZ, MAX_SZ)
                )
                .addGap(8)
        );
        setLayout(gl);
        pack(); // "упаковывает" компоненты в окно
    }

    /**
     * Метод для настройки панели и обработки событий
     */
    private void initializeComponents() {
        mainPanel = new PaintPanel();

        // Обработчик выбора прямоугольника мышью (для увеличения)
        mainPanel.addSelectListener(r -> {
            if (r != null) {
                try {
                    zoomToRectangle(r);
                } catch (InvalidRectException e) {

                }
            }
        });
        setupUndoHandler();
        setupMovementHandlers();
        mainPanel.setBackground(Color.WHITE);

        // Устанавливаем, что рисовать при обновлении панели
        mainPanel.setPaintAction(g -> fp.paint(g));

        // Изменение размеров окна → пересчёт области рисования
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                // Получаем текущие размеры панели
                int newWidth = mainPanel.getWidth();
                int newHeight = mainPanel.getHeight();

                // Устанавливаем новые физические размеры конвертера
                conv.setWidth(newWidth);
                conv.setHeight(newHeight);

                // Сохраняем текущий центр области
                double centerX = (conv.getxMin() + conv.getxMax()) / 2;
                double centerY = (conv.getyMin() + conv.getyMax()) / 2;

                // Вычисляем текущий и новый aspect ratio
                double currentAspect = (conv.getxMax() - conv.getxMin()) / (conv.getyMax() - conv.getyMin());
                double newAspect = (double)newWidth / newHeight;

                // Корректируем границы для сохранения пропорций
                if (newAspect > currentAspect) {
                    // Новая ширина больше - расширяем по Y
                    double newHeightRange = (conv.getxMax() - conv.getxMin()) / newAspect;
                    conv.setyMin(centerY - newHeightRange / 2);
                    conv.setyMax(centerY + newHeightRange / 2);
                } else {
                    // Новая высота больше - расширяем по X
                    double newWidthRange = (conv.getyMax() - conv.getyMin()) * newAspect;
                    conv.setxMin(centerX - newWidthRange / 2);
                    conv.setxMax(centerX + newWidthRange / 2);
                }

                mainPanel.repaint();
            }
        });
    }

    private void setupMovementHandlers() {
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (firstPoint == null) {
                        firstPoint = e.getPoint();
                        System.out.println("Первая точка установлена: " + firstPoint);
                    } else {
                        secondPoint = e.getPoint();
                        System.out.println("Вторая точка установлена: " + secondPoint);
                        calculateAndMoveBounds();
                        // Сбрасываем точки для нового измерения
                        firstPoint = null;
                        secondPoint = null;
                    }
                }
            }
        });
    }

    private void calculateAndMoveBounds() {
        if (firstPoint == null || secondPoint == null) return;

        // Вычисляем разницу между точками в экранных координатах
        double dxScreen = secondPoint.getX() - firstPoint.getX();
        double dyScreen = secondPoint.getY() - firstPoint.getY();

        // Преобразуем разницу в мировые координаты
        double dxWorld = conv.xScr2Crt((int)(firstPoint.getX() + dxScreen)) - conv.xScr2Crt((int)firstPoint.getX());
        double dyWorld = conv.yScr2Crt((int)(firstPoint.getY() + dyScreen)) - conv.yScr2Crt((int)firstPoint.getY());

        // Применяем смещение к текущим границам
        conv.setxMin(conv.getxMin() - dxWorld);
        conv.setxMax(conv.getxMax() - dxWorld);
        conv.setyMin(conv.getyMin() - dyWorld);
        conv.setyMax(conv.getyMax() - dyWorld);

        mainPanel.repaint();
    }


    private void setupUndoHandler() {
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                "undoZoom");

        mainPanel.getActionMap().put("undoZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoZoom();
            }
        });
    }

    private void resizeWindows(Rect r) throws  InvalidRectException {

    }
    private void zoomToRectangle(Rect r) throws InvalidRectException {
        // Сохраняем текущее состояние перед изменением
        saveCurrentView();

        // Применяем новое приближение
        var xMin = conv.xScr2Crt(r.getX());
        var xMax = conv.xScr2Crt(r.getX() + r.getWidth());
        var yMin = conv.yScr2Crt(r.getY() + r.getHeight());
        var yMax = conv.yScr2Crt(r.getY());

        long Width = getWidth();
        long Height = getHeight();
        System.out.println("Высота: " + Height);
        System.out.println("Ширина: " + Width);
        if (xMax-xMin > yMax-yMin) {
            conv.setxMax(xMax);
            conv.setxMin(xMin);
            yMin = yMax-Height*(xMax-xMin)/Width;
            conv.setyMin(yMin);
            conv.setyMax(yMax);
            System.out.println("1) " );
            System.out.println("xMin: " + xMin);
            System.out.println("xMax: " + xMax);
            System.out.println("yMin: " + yMin);
            System.out.println("yMax: " + yMax);
        }
        else {
            conv.setyMin(yMin);
            conv.setyMax(yMax);
            xMax = (yMax - yMin)*Width/Height + xMin;
            conv.setxMax(xMax);
            conv.setxMin(xMin);
            System.out.println("2) " );
            System.out.println("xMin: " + xMin);
            System.out.println("xMax: " + xMax);
            System.out.println("yMin: " + yMin);
            System.out.println("yMax: " + yMax);
        }


        mainPanel.repaint();
    }

    private void saveCurrentView() {
        viewHistory.push(new ViewState(
                conv.getxMin(),
                conv.getxMax(),
                conv.getyMin(),
                conv.getyMax()
        ));

        // Ограничиваем размер истории (например, последние 20 действий)
        if (viewHistory.size() > 100) {
            viewHistory.remove(0);
        }
    }

    private void undoZoom() {
        if (!viewHistory.isEmpty()) {
            ViewState previous = viewHistory.pop();
            conv.setxMin(previous.xMin);
            conv.setxMax(previous.xMax);
            conv.setyMin(previous.yMin);
            conv.setyMax(previous.yMax);
            mainPanel.repaint();
        } else {
            Toolkit.getDefaultToolkit().beep(); // Звуковой сигнал, если нечего отменять
        }
    }




    /**
     * Открытие диалога сохранения с выбором формата:
     * - .jpg или .png → сохранить изображение;
     * - .frac → сохранить координаты.
     */
    private void saveFileUnified() {
        JFileChooser chooser = new JFileChooser();

        // Фильтры для различных типов файлов
        var jpgFilter = new FileNameExtensionFilter("Файлы формата jpeg", "jpg");
        var pngFilter = new FileNameExtensionFilter("Файлы формата png", "png");
        var fracFilter = new FileNameExtensionFilter("Файлы собственного формата frac", "frac");

        chooser.addChoosableFileFilter(jpgFilter);
        chooser.addChoosableFileFilter(pngFilter);
        chooser.addChoosableFileFilter(fracFilter);
        chooser.setFileFilter(jpgFilter); // фильтр по умолчанию
        chooser.setAcceptAllFileFilterUsed(false); // отключаем "All Files"

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        var filter = chooser.getFileFilter();

        try {
            if (filter == fracFilter) {
                // Сохраняем координаты области в .frac
                double xMin = conv.xScr2Crt(0);
                double xMax = conv.xScr2Crt(mainPanel.getWidth());
                double yMax = conv.yScr2Crt(0);
                double yMin = conv.yScr2Crt(mainPanel.getHeight());

                // Добавляем расширение .frac, если его нет
                if (!file.getName().toLowerCase().endsWith(".frac")) {
                    file = new File(file.getAbsolutePath() + ".frac");
                }

                FracIO.saveFrac(file, xMin, xMax, yMin, yMax);

            } else {
                // Сохраняем изображение
                BufferedImage img = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                fp.paint(g2); // рисуем фрактал в изображение

                // Подписи координат
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.drawString(String.format("xMin = %.2f", conv.xScr2Crt(0)), 10, img.getHeight() - 25);
                g2.drawString(String.format("xMax = %.2f", conv.xScr2Crt(mainPanel.getWidth())), img.getWidth() - 100, img.getHeight() - 25);
                g2.drawString(String.format("yMin = %.2f", conv.yScr2Crt(mainPanel.getHeight())), 10, img.getHeight() - 10);
                g2.drawString(String.format("yMax = %.2f", conv.yScr2Crt(0)), 10, 15);
                g2.dispose();

                // Добавление расширения .jpg или .png
                String ext = filter == pngFilter ? ".png" : ".jpg";
                if (!file.getName().toLowerCase().endsWith(ext)) {
                    file = new File(file.getAbsolutePath() + ext);
                }

                FileData.saveAsImage(img, file);
            }

        } catch (IOException | NonImageFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении файла: " + ex.getMessage());
        }
    }

    /**
     * Загрузка сохранённого .frac файла с координатами области фрактала.
     * После загрузки фрактал отрисовывается в том же виде, что и при сохранении.
     */
    private void openFracFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter fracFilter = new FileNameExtensionFilter("Файлы формата .frac", "frac");
        chooser.setFileFilter(fracFilter);
        chooser.setAcceptAllFileFilterUsed(false); // скрываем "All Files"

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                // Загружаем координаты из файла
                Map<String, Double> map = FracIO.loadFrac(file);
                conv.setxMin(map.get("xMin"));
                conv.setxMax(map.get("xMax"));
                conv.setyMin(map.get("yMin"));
                conv.setyMax(map.get("yMax"));

                mainPanel.repaint(); // перерисовываем фрактал
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке .frac файла.");
            }
        }
    }
}
