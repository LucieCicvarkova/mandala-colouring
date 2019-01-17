
package cz.czechitas.mandala;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import net.miginfocom.swing.*;
import net.sevecek.util.*;

public class HlavniOkno extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    JLabel labAktualniBarva;
    JLabel labCervena;
    JLabel labZluta;
    JLabel labBila;
    JLabel labZelena;
    JLabel labModra;
    JButton btnReset;
    JButton btnNacistObrazek;
    JButton btnUlozObrazek;
    JLabel labObrazek;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    JPanel contentPane;
    MigLayout migLayoutManager;
    BufferedImage obrazek;
    File otevrenySoubor;
    Color barva;
    boolean obrazekZeSouboru;

    public HlavniOkno() {
        initComponents();
        nahrajObrazek(null);

    }

    //nahrani defaultniho obrazku ze souboru

/* private void nahrajObrazek() {
        try {
            InputStream soubor = this.getClass().getResourceAsStream("/mandala2cb.jpg");
            obrazek = ImageIO.read(soubor);
            labObrazek.setIcon(new ImageIcon(obrazek));
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se nahrát obrázek mandaly ze souboru" + soubor.getAbsolutePath());
        }
    }
    Tahle varianta nefunguje, hlasi chybu v  + soubor.getAbsolutePath()
     */

private void nahrajObrazek(File soubor) {
        if (soubor == null) {
            try {
                obrazek = ImageIO.read(getClass().getResourceAsStream("/mandala2cb.jpg"));
            } catch (IOException ex) {
            }
        } else {
            try {
                obrazek = ImageIO.read(soubor);
            } catch (IOException ex) {
                throw new ApplicationPublicException(ex, "Nepodařilo se nahrát obrázek mandaly ze souboru " + soubor.getAbsolutePath());
            }
        }
       labObrazek.setIcon(new ImageIcon(obrazek));
        labObrazek.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
        pack();
        setMinimumSize(getSize());
    }

   
    //ulozeni hotoveho obrazku:

    private void ulozitJako() {
        JFileChooser dialog;
        dialog = new JFileChooser(".");

        int vysledek = dialog.showSaveDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File soubor = dialog.getSelectedFile();
        if (!soubor.getName().contains(".") && !soubor.exists()) {
            soubor = new File(soubor.getParentFile(), soubor.getName() + ".png");
        }
        if (soubor.exists()) {
            int potvrzeni = JOptionPane.showConfirmDialog(this, "Soubor " + soubor.getName() + " už existuje.\nChcete jej přepsat?", "Přepsat soubor?", JOptionPane.YES_NO_OPTION);
            if (potvrzeni == JOptionPane.NO_OPTION) {
                return;
            }
        }
        ulozObrazek(soubor);
    }

    private void ulozObrazek(File soubor) {
        try {
            ImageIO.write(obrazek, "png", soubor);
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se uložit obrázek mandaly do souboru " + soubor.getAbsolutePath());
        }
    }

    //nahrani jineho obrazku ze souboru:


    private void nacistObrazekZeSouboru(ActionEvent e) {
        JFileChooser dialog;
        if (otevrenySoubor == null) {
            dialog = new JFileChooser(".");
        } else {
            dialog = new JFileChooser(otevrenySoubor.getParentFile());
        }
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setMultiSelectionEnabled(false);
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("Obrázky (*.png)", "png", "jpg", "(*.jpg)"));
        int vysledek = dialog.showOpenDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        otevrenySoubor = dialog.getSelectedFile();
        nahrajObrazek(otevrenySoubor);

        obrazekZeSouboru = true;

    }

   //vybarvovani klikanim mysi

    private void kliknutiNaLabObrazek(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        vyplnObrazek(obrazek, x, y,barva);
        labObrazek.repaint();
    }


    //metody na vyplnovani barvou:

    public void vyplnObrazek(BufferedImage obrazek, int x, int y, Color barva) {
        if (barva == null) {
            barva = new Color(255, 255, 0);
        }

        // Zamez vyplnovani mimo rozsah
        if (x < 0 || x >= obrazek.getWidth() || y < 0 || y >= obrazek.getHeight()) {
            return;
        }

        WritableRaster pixely = obrazek.getRaster();
        int[] novyPixel = new int[] {barva.getRed(), barva.getGreen(), barva.getBlue(), barva.getAlpha()};
        int[] staryPixel = new int[] {255, 255, 255, 255};
        staryPixel = pixely.getPixel(x, y, staryPixel);

        // Pokud uz je pocatecni pixel obarven na cilovou barvu, nic nemen
        if (pixelyMajiStejnouBarvu(novyPixel, staryPixel)) {
            return;
        }

        // Zamez prebarveni cerne cary
        int[] cernyPixel = new int[] {0, 0, 0, 0};
        if (pixelyMajiStejnouBarvu(cernyPixel, staryPixel)) {
            return;
        }

        vyplnovaciSmycka(pixely, x, y, novyPixel, staryPixel);
    }

    private void vyplnovaciSmycka(WritableRaster raster, int x, int y, int[] novaBarva, int[] nahrazovanaBarva) {
        Rectangle rozmery = raster.getBounds();
        int[] aktualniBarva = new int[] {255, 255, 255, 255};

        Deque<Point> zasobnik = new ArrayDeque<>(rozmery.width * rozmery.height);
        zasobnik.push(new Point(x, y));
        while (zasobnik.size() > 0) {
            Point point = zasobnik.pop();
            x = point.x;
            y = point.y;
            if (!pixelyMajiStejnouBarvu(raster.getPixel(x, y, aktualniBarva), nahrazovanaBarva)) {
                continue;
            }

            // Najdi levou zed, po ceste vyplnuj
            int levaZed = x;
            do {
                raster.setPixel(levaZed, y, novaBarva);
                levaZed--;
            }
            while (levaZed >= 0 && pixelyMajiStejnouBarvu(raster.getPixel(levaZed, y, aktualniBarva), nahrazovanaBarva));
            levaZed++;

            // Najdi pravou zed, po ceste vyplnuj
            int pravaZed = x;
            do {
                raster.setPixel(pravaZed, y, novaBarva);
                pravaZed++;
            }
            while (pravaZed < rozmery.width && pixelyMajiStejnouBarvu(raster.getPixel(pravaZed, y, aktualniBarva), nahrazovanaBarva));
            pravaZed--;

            // Pridej na zasobnik body nahore a dole
            for (int i = levaZed; i <= pravaZed; i++) {
                if (y > 0 && pixelyMajiStejnouBarvu(raster.getPixel(i, y - 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y - 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y - 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y - 1));
                    }
                }
                if (y < rozmery.height - 1 && pixelyMajiStejnouBarvu(raster.getPixel(i, y + 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y + 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y + 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y + 1));
                    }
                }
            }
        }
    }

    private boolean pixelyMajiStejnouBarvu(int[] barva1, int[] barva2) {
        return barva1[0] == barva2[0] && barva1[1] == barva2[1] && barva1[2] == barva2[2];
    }



    //obsluha tlacitek:

    private void btnUlozObrazek(ActionEvent e) {
        ulozitJako();
    }

    private void btnNacistObrazekZeSouboru(ActionEvent e) {
        nacistObrazekZeSouboru(null);
    }

    private void btnReset(ActionEvent e) {
        if (obrazekZeSouboru) {
            nahrajObrazek(otevrenySoubor);
        } else if (!obrazekZeSouboru) {
            nahrajObrazek(null);
        }
    }

    //barvy
    // tm.modra (20, 30, 120),  zluta   (247, 217, 70), cervena (200, 30, 20), zelena (35, 85, 7)

    private void klikModra(MouseEvent e) {
        labModra.setText("X");
        labZluta.setText("");
        labCervena.setText("");
        labZelena.setText("");

        barva = new Color(20, 30, 120);
    }

    private void klikZluta(MouseEvent e) {
        labModra.setText("");
        labZluta.setText("X");
        labCervena.setText("");
        labZelena.setText("");
        labBila.setText("");

        barva =  new Color(247, 217, 70);
    }

    private void klikCervena(MouseEvent e) {
        labModra.setText("");
        labZluta.setText("");
        labCervena.setText("X");
        labZelena.setText("");
        labBila.setText("");

        barva = new Color(200, 30, 20);

    }

    private void klikZelena(MouseEvent e) {
        labModra.setText("");
        labZluta.setText("");
        labCervena.setText("");
        labZelena.setText("X");
        labBila.setText("");

        barva = new Color(35, 85, 7);
    }

    private void klikBila(MouseEvent e) {
        labModra.setText("");
        labZluta.setText("");
        labCervena.setText("");
        labZelena.setText("");
        labBila.setText("X");

        barva = new Color(250, 250, 250);
    }




    private void initComponents(){
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        labAktualniBarva = new JLabel();
        labCervena = new JLabel();
        labZluta = new JLabel();
        labBila = new JLabel();
        labZelena = new JLabel();
        labModra = new JLabel();
        btnReset = new JButton();
        btnNacistObrazek = new JButton();
        btnUlozObrazek = new JButton();
        labObrazek = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mandala");
        setMinimumSize(new Dimension(700, 800));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                klikModra(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets rel,hidemode 3",
            // columns
            "[fill]" +
            "[87,fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[30,fill]" +
            "[30,fill]" +
            "[31,grow,fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[fill]" +
            "[]" +
            "[fill]" +
            "[]"));
        this.contentPane = (JPanel) this.getContentPane();
        this.contentPane.setBackground(this.getBackground());
        LayoutManager layout = this.contentPane.getLayout();
        if (layout instanceof MigLayout) {
            this.migLayoutManager = (MigLayout) layout;
        }

        //---- labAktualniBarva ----
        labAktualniBarva.setText("V\u00fdb\u011br barvy");
        contentPane.add(labAktualniBarva, "cell 0 0");

        //---- labCervena ----
        labCervena.setBackground(new Color(200, 30, 20));
        labCervena.setOpaque(true);
        labCervena.setMaximumSize(new Dimension(32, 32));
        labCervena.setMinimumSize(new Dimension(32, 32));
        labCervena.setBorder(new BevelBorder(BevelBorder.RAISED));
        labCervena.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labCervena.setForeground(Color.white);
        labCervena.setHorizontalAlignment(SwingConstants.CENTER);
        labCervena.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                klikCervena(e);
            }
        });
        contentPane.add(labCervena, "cell 0 0");

        //---- labZluta ----
        labZluta.setBackground(new Color(247, 217, 70));
        labZluta.setOpaque(true);
        labZluta.setMaximumSize(new Dimension(32, 32));
        labZluta.setMinimumSize(new Dimension(32, 32));
        labZluta.setHorizontalTextPosition(SwingConstants.CENTER);
        labZluta.setBorder(new BevelBorder(BevelBorder.RAISED));
        labZluta.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labZluta.setHorizontalAlignment(SwingConstants.CENTER);
        labZluta.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                klikZluta(e);
            }
        });
        contentPane.add(labZluta, "cell 0 0");

        //---- labBila ----
        labBila.setBackground(Color.white);
        labBila.setOpaque(true);
        labBila.setMaximumSize(new Dimension(32, 32));
        labBila.setMinimumSize(new Dimension(32, 32));
        labBila.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBila.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labBila.setHorizontalAlignment(SwingConstants.CENTER);
        labBila.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                klikZelena(e);
                klikBila(e);
            }
        });
        contentPane.add(labBila, "cell 1 0");

        //---- labZelena ----
        labZelena.setBackground(new Color(35, 85, 7));
        labZelena.setOpaque(true);
        labZelena.setMaximumSize(new Dimension(32, 32));
        labZelena.setMinimumSize(new Dimension(32, 32));
        labZelena.setBorder(new BevelBorder(BevelBorder.RAISED));
        labZelena.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labZelena.setForeground(Color.white);
        labZelena.setHorizontalAlignment(SwingConstants.CENTER);
        labZelena.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                klikZelena(e);
            }
        });
        contentPane.add(labZelena, "cell 1 0");

        //---- labModra ----
        labModra.setBackground(new Color(20, 30, 120));
        labModra.setOpaque(true);
        labModra.setBorder(new BevelBorder(BevelBorder.RAISED));
        labModra.setHorizontalTextPosition(SwingConstants.CENTER);
        labModra.setMaximumSize(new Dimension(32, 32));
        labModra.setMinimumSize(new Dimension(32, 32));
        labModra.setHorizontalAlignment(SwingConstants.CENTER);
        labModra.setForeground(Color.white);
        labModra.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labModra.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                klikModra(e);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                klikModra(e);
            }
        });
        contentPane.add(labModra, "cell 1 0");

        //---- btnReset ----
        btnReset.setText("Reset obr\u00e1zku");
        btnReset.addActionListener(e -> btnReset(e));
        contentPane.add(btnReset, "cell 2 0 5 1");

        //---- btnNacistObrazek ----
        btnNacistObrazek.setText("Na\u010d\u00edst nov\u00fd obr\u00e1zek");
        btnNacistObrazek.setActionCommand("Na\u010d\u00edst obr\u00e1zek");
        btnNacistObrazek.addActionListener(e -> btnNacistObrazekZeSouboru(e));
        contentPane.add(btnNacistObrazek, "cell 0 1");

        //---- btnUlozObrazek ----
        btnUlozObrazek.setText("Ulo\u017eit obr\u00e1zek");
        btnUlozObrazek.addActionListener(e -> btnUlozObrazek(e));
        contentPane.add(btnUlozObrazek, "cell 1 1 4 1");

        //---- labObrazek ----
        labObrazek.setOpaque(true);
        labObrazek.setBackground(Color.white);
        labObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                kliknutiNaLabObrazek(e);
            }
        });
        contentPane.add(labObrazek, "cell 0 2 11 3");
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
