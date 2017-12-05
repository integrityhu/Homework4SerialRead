package hu.infokristaly.homework4serialread;

import com.sun.glass.events.KeyEvent;
import java.awt.AWTException;
import java.awt.Robot;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * @see https://stackoverflow.com/questions/336714/reading-serial-port-in-java
 * @author pzoli
 */
public class FXMLController implements Initializable, jssc.SerialPortEventListener {

    private SerialPort serialPort;
    private java.awt.Robot robot;

    @FXML
    private ComboBox serialPortList;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (serialPortList.getValue() != null) {
            if (serialPort == null) {
                serialPort = new SerialPort(serialPortList.getValue().toString());
            }
            if (!serialPort.isOpened()) {
                try {
                    //Open port
                    serialPort.openPort();
                    //We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
                    serialPort.setParams(SerialPort.BAUDRATE_9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    int mask = SerialPort.MASK_RXCHAR;
                    serialPort.setEventsMask(mask);
                    serialPort.addEventListener(this);
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {
            int count = event.getEventValue();
            if (count > 0) {
                try {
                    byte buffer[] = serialPort.readBytes(count);
                    String input = new String(buffer); //"distance:50 mm" 
                    String[] splitDistanceLine = input.split(":");
                    if (splitDistanceLine.length == 2) {
                        String[] distanceValue = splitDistanceLine[1].split(" ");
                        if ((distanceValue.length == 2) && (Integer.valueOf(distanceValue[0])<=50)) { //less then or equal 5 cm
                            robot.keyPress(KeyEvent.VK_E);
                            Thread.sleep(200);
                            robot.keyRelease(KeyEvent.VK_E);
                        }
                    }
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            robot = new Robot();
            String[] portNames = SerialPortList.getPortNames();
            for (int i = 0; i < portNames.length; i++) {
                serialPortList.getItems().add(new String(portNames[i]));
            }
        } catch (AWTException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        closeSerialPort();
        super.finalize();
    }

    void closeSerialPort() {
        if ((serialPort != null) && serialPort.isOpened()) {
            try {
                serialPort.closePort();
            } catch (SerialPortException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
