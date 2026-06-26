package Controllers;

import Models.ExecutionMode;
import Models.HomePageModel;
import javax.swing.Timer;
import screens.scenes.ElevatorSimulationScenePanel;

public class ElevatorSimulatorController {

    // Timers de animação
    private Timer motorUpTimer;
    private Timer motorDownTimer;
    private Timer doorCloseTimer;
    private Timer doorOpenTimer;

    // --- 🛠️ FÍSICA SINCRONIZADA COM A INTERFACE 🛠️ ---
    // Y_ANDAR_1 DEVE ser o mesmo yAndar1 do painel (251)
    private final int Y_ANDAR_1 = 251;
    // Y_ANDAR_2 DEVE ser o mesmo yAndar2 do painel (50)
    private final int Y_ANDAR_2 = 50;
    
    // OFFSET_OPEN DEVE ser igual ou ligeiramente maior que o wDoor (40)
    private final int OFFSET_OPEN = 40;
    private final int OFFSET_CLOSED = 0;
    private final int SPEED = 2;

    // Posições Iniciais Dinâmicas
    private int cabinY = Y_ANDAR_1;          // Começa no A1
    private int doorOffsetA1 = OFFSET_OPEN;  // Porta A1 começa aberta
    private int doorOffsetA2 = OFFSET_CLOSED;// Porta A2 começa fechada

    public boolean motorDamaged = false;
    private final ElevatorSimulationScenePanel panel;

    public ElevatorSimulatorController(ElevatorSimulationScenePanel panel) {
        this.panel = panel;
    }

    public int getCabinY() {
        return cabinY;
    }
    
    public int getDoorOffsetA1() { return doorOffsetA1; }
    public int getDoorOffsetA2() { return doorOffsetA2; }

    public boolean isDoorFullyClosed() {
        if (cabinY >= Y_ANDAR_1) return doorOffsetA1 == OFFSET_CLOSED;
        if (cabinY <= Y_ANDAR_2) return doorOffsetA2 == OFFSET_CLOSED;
        return true;
    }

    public boolean isMotorRunning() {
        return (motorUpTimer != null && motorUpTimer.isRunning()) ||
               (motorDownTimer != null && motorDownTimer.isRunning());
    }

    // --- MOVIMENTO DA CABINE ---
    public void startCabinUp() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopCabinUp();
            return;
        }
        if (motorUpTimer != null && motorUpTimer.isRunning()) return;

        motorUpTimer = new Timer(15, e -> {
            if (cabinY > Y_ANDAR_2) {
                cabinY -= SPEED;
                panel.repaint();
            } else {
                // Chegou exatamente no A2
                cabinY = Y_ANDAR_2; 
                stopCabinUp();
                panel.repaint();
            }
        });
        motorUpTimer.start();
    }

    public void startCabinDown() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopCabinDown();
            return;
        }
        if (motorDownTimer != null && motorDownTimer.isRunning()) return;

        motorDownTimer = new Timer(15, e -> {
            if (cabinY < Y_ANDAR_1) {
                cabinY += SPEED;
                panel.repaint();
            } else {
                // Chegou exatamente no A1
                cabinY = Y_ANDAR_1;
                stopCabinDown();
                panel.repaint();
            }
        });
        motorDownTimer.start();
    }

    public void stopCabinUp() {
        if (motorUpTimer != null && motorUpTimer.isRunning()) motorUpTimer.stop();
    }

    public void stopCabinDown() {
        if (motorDownTimer != null && motorDownTimer.isRunning()) motorDownTimer.stop();
    }

    // --- MOVIMENTO DAS PORTAS ---
    public void startClosingDoors() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopClosingDoors();
            return;
        }
        if (doorCloseTimer != null && doorCloseTimer.isRunning()) return;
        stopOpeningDoors(); 

        doorCloseTimer = new Timer(15, e -> {
            boolean moved = false;
            if (cabinY >= Y_ANDAR_1 && doorOffsetA1 > OFFSET_CLOSED) { doorOffsetA1 -= 1; moved = true; }
            if (cabinY <= Y_ANDAR_2 && doorOffsetA2 > OFFSET_CLOSED) { doorOffsetA2 -= 1; moved = true; }
            
            if (moved) {
                panel.repaint(); 
            } else {
                stopClosingDoors();
            }
        });
        doorCloseTimer.start();
    }

    public void startOpeningDoors() {
        if (HomePageModel.getMode() != ExecutionMode.RUNNING) {
            stopOpeningDoors();
            return;
        }
        if (doorOpenTimer != null && doorOpenTimer.isRunning()) return;
        stopClosingDoors();

        doorOpenTimer = new Timer(15, e -> {
            boolean moved = false;
            if (cabinY >= Y_ANDAR_1 && doorOffsetA1 < OFFSET_OPEN) { doorOffsetA1 += 1; moved = true; }
            if (cabinY <= Y_ANDAR_2 && doorOffsetA2 < OFFSET_OPEN) { doorOffsetA2 += 1; moved = true; }
            
            if (moved) {
                panel.repaint(); 
            } else {
                stopOpeningDoors();
            }
        });
        doorOpenTimer.start();
    }

    public void stopClosingDoors() {
        if (doorCloseTimer != null && doorCloseTimer.isRunning()) doorCloseTimer.stop();
    }

    public void stopOpeningDoors() {
        if (doorOpenTimer != null && doorOpenTimer.isRunning()) doorOpenTimer.stop();
    }

    // --- CONTROLE DE FALHAS ---
    public void reset() {
        stopCabinUp();
        stopCabinDown();
        stopClosingDoors();
        stopOpeningDoors();
        
        // Retorna aos estados definidos no bloco de cima
        cabinY = Y_ANDAR_1;
        doorOffsetA1 = OFFSET_OPEN;
        doorOffsetA2 = OFFSET_CLOSED;
        motorDamaged = false;
        
        panel.repaint();
    }

    public void triggerDamage() {
        motorDamaged = true;
        stopCabinUp();
        stopCabinDown();
        panel.repaint();
    }
}