package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

public class ComplexCellRenderer extends JLabel implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        Font theFont = null;
        Color theForeground = null;
        String theText = null;
        Icon image = null;
        String event;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.now();
//        System.out.println(dtf.format(localDate));

        if(localDate.getMonth()== Month.DECEMBER){
            event = "christmas/";
        }else if(localDate.getMonth()==Month.OCTOBER){
            event = "halloween/";
        }else{
            event = "large/";
        }




        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);
        try{
            if (value instanceof Object[]) {
                Object values[] = (Object[]) value;
                theText = (String) values[1];
                image = new ImageIcon(getClass().getResource(event + theText));
                //image = ImageIO.read(Main.class.getResource("large/" + (String) values[1]));
            } else {
                theFont = list.getFont();
                theForeground = list.getForeground();
                theText = "no Text";
            }
            if (!isSelected) {
                renderer.setForeground(theForeground);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        renderer.setText(null);
        renderer.setFont(null);
        renderer.setIcon(image);

        return renderer;
    }
}
