package Controllers;

import Models.ExecutionMode;
import Models.HomePageModel;
import javax.swing.Timer;
import screens.scenes.GarageSimulationScenePanel;

public class GarageSimulatorController {

    private Timer motorUpTimer;
    private Timer motorDownTimer;

    // Configurações do Portão
    private int doorHeight = 330;
    private final int HEIGHT_OPEN = 0;
    private final int HEIGHT_CLOSED = 330;
    private final int SPEED = 2;

    public boolean motorDamaged = false;

    private final GarageSimulationScenePanel panel;

    public GarageSimulatorController(GarageSimulationScenePanel panel) {
        this.panel = panel;
    }

    public int getDoorY() {
        return doorHeight;
    }

    // --- Novo método para checar status do motor ---
    public boolean isMotorRunning() {
        return (motorUpTimer != null && motorUpTimer.isRunning()) ||
                (motorDownTimer != null && motorDownTimer.isRunning());
    }

    public void startOpening() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopOpening();
            return;
        }
        if (motorUpTimer != null && motorUpTimer.isRunning())
            return;

        motorUpTimer = new Timer(15, e -> {
            if (doorHeight > HEIGHT_OPEN) {
                doorHeight -= SPEED;
                panel.repaint();
            } else {
                stopOpening();
            }
        });
        motorUpTimer.start();
    }

    public void startClosing() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopClosing();
            return;
        }
        if (motorDownTimer != null && motorDownTimer.isRunning())
            return;

        motorDownTimer = new Timer(15, e -> {
            if (doorHeight < HEIGHT_CLOSED) {
                doorHeight += SPEED;
                panel.repaint();
            } else {
                stopClosing();
            }
        });
        motorDownTimer.start();
    }

    public void stopOpening() {
        if (motorUpTimer != null && motorUpTimer.isRunning())
            motorUpTimer.stop();
    }

    public void stopClosing() {
        if (motorDownTimer != null && motorDownTimer.isRunning())
            motorDownTimer.stop();
    }

    public void reset() {
        stopOpening();
        stopClosing();
        doorHeight = HEIGHT_CLOSED;
        motorDamaged = false;
        panel.repaint();
    }

    public void triggerDamage() {
        motorDamaged = true;
        stopOpening();
        stopClosing();
        panel.repaint();
    }
}