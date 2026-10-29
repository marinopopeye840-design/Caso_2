
package ni.edu.uam.caso_2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class PedidoController {

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnCancelar;

    @FXML
    private ProgressBar barraProgreso;

    @FXML
    private Label lblContador;

    @FXML
    private Label lblEstado;

    private Thread hiloPedidos;
    private boolean cancelacionSolicitada = false;
    private boolean cerrando = false;

    @FXML
    public void initialize() {
        barraProgreso.setProgress(0);
        lblContador.setText("Pedidos Completados: 0 / 30");
        lblEstado.setText("Listo");

        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);
    }

    @FXML
    private void iniciarPedidos() {

        if (hiloPedidos != null && hiloPedidos.isAlive()) {
            return;
        }

        ProcesadorPedido procesador = new ProcesadorPedido();
        cancelacionSolicitada = false;

        barraProgreso.setProgress(0);
        lblContador.setText("Pedidos Completados: 0 / 30");
        lblEstado.setText("En ejecución");

        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        hiloPedidos = new Thread(() -> {

            boolean error = false;

            try {
                for (int i = 0; i < procesador.getTotalPedidos(); i++) {

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    Thread.sleep(200);

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    procesador.completarPedido();

                    int completados = procesador.getPedidoCompletos();
                    double progreso = (double) completados
                            / procesador.getTotalPedidos();

                    Platform.runLater(() -> {
                        if (!cerrando) {
                            lblContador.setText(
                                    "Pedidos Completados: " + completados + " / 30"
                            );
                            barraProgreso.setProgress(progreso);
                        }
                    });
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                error = true;
                e.printStackTrace();
            }

            boolean huboError = error;

            Platform.runLater(() -> {
                if (!cerrando) {
                    lblEstado.setText("Cancelando");
                    btnCancelar.setDisable(true);
                }
            });

            Thread hiloActual = Thread.currentThread();

            Thread espera = new Thread(() -> {
                try {
                    hiloActual.join();

                    Platform.runLater(() -> {
                        if (cerrando) {
                            return;
                        }

                        if (huboError) {
                            lblEstado.setText("Error");
                        } else if (cancelacionSolicitada) {
                            lblEstado.setText("Cancelado");
                        } else {
                            lblEstado.setText("Completado");
                        }

                        btnIniciar.setDisable(false);
                        btnCancelar.setDisable(true);
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            espera.setDaemon(true);
            espera.start();
        });

        hiloPedidos.setDaemon(true);
        hiloPedidos.start();
    }

    @FXML
    private void cancelarPedidos() {

        if (hiloPedidos != null && hiloPedidos.isAlive()) {

            cancelacionSolicitada = true;
            lblEstado.setText("Cancelando");

            btnIniciar.setDisable(true);
            btnCancelar.setDisable(true);

            hiloPedidos.interrupt();
        }
    }

    public void cerrar(Runnable alTerminar) {

        cerrando = true;

        Thread hiloActual = hiloPedidos;

        if (hiloActual == null || !hiloActual.isAlive()) {
            alTerminar.run();
            return;
        }

        cancelacionSolicitada = true;
        hiloActual.interrupt();

        Thread espera = new Thread(() -> {

            try {

                hiloActual.join();

                Platform.runLater(alTerminar);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });

        espera.setDaemon(true);
        espera.start();
    }
}