package Logica;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;

public class MostrarSimulacion extends JFrame {

    Simulacion laSimulacion = new Simulacion();
    ArrayList<Oveja> lasOvejas = new ArrayList<>();
    ArrayList<Lobo> losLobos = new ArrayList<>();
    ArrayList<Pasto> elPasto = new ArrayList<>();
    Thread hiloUno;

    Thread hiloSegundos; // este hilo controla el contador de segundos de la simulacion global
    JFrame VentanaSegundos;
    JTextField textoSegundos;

    Graphics buffer;
    BufferedImage dibujo;
    Image ovejo = new ImageIcon(getClass().getResource("/img/ovejo.png")).getImage();
    Image oveja = new ImageIcon(getClass().getResource("/img/oveja.png")).getImage();
    Image lobo = new ImageIcon(getClass().getResource("/img/lobo.png")).getImage();
    Image pasto = new ImageIcon(getClass().getResource("/img/pasto.png")).getImage();
    Image fondo = new ImageIcon(getClass().getResource("/img/fondo.png")).getImage();
    int cantOvejas;
    int cantLobos;
    int velocidadOvejas = 2; //velocidad de las ovejas
    int velocidadLobos = 2; //velocidad de los lobos

    /////////////////////////////
    //autor: jonathan loaiza rosero
    boolean horaDeComer = false;  //variable para activar el movimiento de un lobo hacia su comida
    int tiempoLobosAlimento;     //guarda la cantidad de tiempo que tiene que transcurrir para que un lobo se coma una oveja
    int loboAleatorio;          // lobo seleccionado aleatoriamente para que se coma una oveja
    int ovejaObjetivo;         // oveja seleccionada para ser comida
    /////////////////////////////
    
    int cantPasto = 5; //cantidad de pasto que quirenque aparezca cada X tiempo
    int maxCantidadPasto; //cantidad máxima de pasto en el ambiente
    int tiempoPasto;

    //tamano de la ventana
    int anchoVentana = 1200;
    int altoVetana = 700;

    //variable Random
    Random r = new Random();

    public MostrarSimulacion() {

        laSimulacion.setTamanoSimulacion(anchoVentana, altoVetana); //trasnfiera el tamano de ventana a los margenes de la simulacion

        cantOvejas = Integer.parseInt(FrameSimulacion.textFieldCantidadO.getText());
        cantLobos = Integer.parseInt(FrameSimulacion.textFieldCantidadL.getText());
        tiempoPasto = Integer.parseInt(FrameSimulacion.textFieldPasto.getText());
        maxCantidadPasto = Integer.parseInt(FrameSimulacion.textFieldCantidadMaxCesped.getText());
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // autor: jonathan loaiza rosero
        tiempoLobosAlimento = Integer.parseInt(FrameSimulacion.textFieldComidaL.getText());  // se obtiene el tiempo apartir un lobo busca su comida
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        lasOvejas = laSimulacion.Oveja(cantOvejas);
        losLobos = laSimulacion.Lobo(cantLobos);
        elPasto = laSimulacion.Pasto(cantPasto);
        setTitle("Lobos y Ovejas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(10, 10, anchoVentana, altoVetana);
        dibujo = new BufferedImage(anchoVentana, altoVetana, BufferedImage.TYPE_INT_RGB);
        buffer = dibujo.createGraphics();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);

        hiloPrincipal();

        repaint();

        //no descomentar ya es esta seccion fue reemplazado por la func hiloPrincipal()
        //la dejo aca solo con fines de entendimiento
        /*hiloUno = new Thread(new Runnable() { 
            
                int segundosEjecucion; // hace las vecs de segundero
                int contador = 0; // cuanta cuantas veces se detien el hilo
                @Override
                public void run() {
                    while (true) {
                        try {
                            moverOveja();
                            moverLobo();
                            repaint();                            
                            
                            if(contador == 20){ 
                                // cuando contador == 20 quiere decir que ha parado 20 veces por un tiempo de 50 milisegundos
                                //lo cual es un segundo (20*50 = 1000 milisegundos), asi que aumenta en un segundo la variable
                                segundosEjecucion++;
                                contador = 0;
                            } 
                            if (segundosEjecucion%3 == 0){
                                elPasto = laSimulacion.Pasto();
                            }
                            contador++;
                            Thread.sleep(50); //se detien por 50 milisegundos,        
                                                                                                               
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });    */
    }

    private void hiloPrincipal() {
        //seccion para mostrar un panel de simulacion de segundos transcurridos para saber
        //en que momento aparece el pasto (por ejmplo)
        //este hilo  tambien debera ejecutar todo a la vez, 
        //creo que esta es la forma de adentro de el, ejecutar los segundos que sean globales a todo

        VentanaSegundos = new JFrame("tiempo");
        VentanaSegundos.setResizable(false);
        VentanaSegundos.setLocation(10, 10);
        VentanaSegundos.setBounds(0, 0, 50, 50);
        textoSegundos = new JTextField("0");
        VentanaSegundos.setBackground(Color.red);
        VentanaSegundos.add(textoSegundos);

        VentanaSegundos.setVisible(true);

        hiloSegundos = new Thread(new Runnable() {

            int segundosEjecucion; // hace las vecs de segundero
            int contador = 0; // cuanta cuantas veces se detiene el hilo

            @Override
            public void run() {
                while (true) {
                    try {
                        moverOveja();
                        if(horaDeComer==false){
                            moverLobo();
                        }

                        if (contador == 20) {
                            // cuando contador == 20 quiere decir que ha parado 20 veces por un tiempo de 50 milisegundos
                            //lo cual es un segundo (20*50 = 1000 milisegundos), asi que aumenta en un segundo la variable segundosEjecucion
                            segundosEjecucion++;
                            contador = 0; // se reinicia el contador

                            System.out.println("segundo: " + segundosEjecucion);
                            textoSegundos.setText("segundo: " + segundosEjecucion);
                            
                            /////////////////////////////////////////////////////////////////////////
                            if(segundosEjecucion % tiempoLobosAlimento == 0 && horaDeComer == false){ // cada X segundos un lobo busca comida, sin embargo un lobo no va a buscar antes que otro termine
                                horaDeComer = true;  
                                loboAleatorio = r.nextInt(5);   // se selecciona el lobo aleatorio
                                ovejaObjetivo = ovejaMasCercana(loboAleatorio); // se encuentra la oveja mas cercana al lobo
                            }
                            
                            if(horaDeComer){
                                    moverLovoHastaOvejaObjetivo();  // movimiento del lobo en busca de comida
                                }
                            
                            //////////////////////////////////////////////////////////////////////////

                            if (segundosEjecucion % tiempoPasto == 0) {//cada X segundos agrega pasto (en este caso 3 segundos)

                                //si la cantidad de pasto alcanza el máximo permitido dejará de producirse 
                                if (elPasto.size() <= maxCantidadPasto) {
                                    agregarPasto();
                                    System.out.println("cantPasto: " + elPasto.size());
                                }
                            }
                        }

                        contador++;
                        Thread.sleep(10); //se detien por 50 milisegundos, 

                        repaint();
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }

    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void update(Graphics g) {
        buffer.drawImage(fondo, 0, 0, this);

        //ovejas
        for (int i = 0; i < lasOvejas.size(); i++) {
            if (i % 2 == 0) {

                //Pinta identificador de la oveja
                g.drawString(String.valueOf(lasOvejas.get(i).getId() + 1), lasOvejas.get(i).getX() + 8, lasOvejas.get(i).getY());
                Font font = new Font("Serif", Font.BOLD, 20);
                g.setFont(font);
                g.setColor(Color.RED);
                //Pinta oveja
                buffer.drawImage(oveja, lasOvejas.get(i).getX(), lasOvejas.get(i).getY(), this);

            } else {
                //Pinta identificador de la oveja
                g.drawString(String.valueOf(lasOvejas.get(i).getId() + 1), lasOvejas.get(i).getX() + 8, lasOvejas.get(i).getY());
                Font font = new Font("Serif", Font.BOLD, 20);
                g.setFont(font);
                g.setColor(Color.RED);
                //Pinta oveja
                buffer.drawImage(ovejo, lasOvejas.get(i).getX(), lasOvejas.get(i).getY(), this);

            }
        }

        //lobos
        for (int j = 0; j < losLobos.size(); j++) {
            buffer.drawImage(lobo, losLobos.get(j).getX(), losLobos.get(j).getY(), this);
        }

        //pasto
        for (int k = 0; k < elPasto.size(); k++) {
            buffer.drawImage(pasto, elPasto.get(k).getX(), elPasto.get(k).getY(), this);
        }
        g.drawImage(dibujo, 0, 0, this);
    }

    public void movimiento() {
        hiloSegundos.start();// inicia el hilo principal
    }

    public void moverOveja() {
        for (int i = 0; i < lasOvejas.size(); i++) {
            int randomX;
            int randomY;

            int[] numerosAleatorios = {-1, 1};
            randomX = r.nextInt(2);
            randomY = r.nextInt(2);

            /*
            *Movimientos aleatorios comprobando los limites del frame
             */
            if (numerosAleatorios[randomX] == 1 && numerosAleatorios[randomY] == 1) {
                lasOvejas.get(i).setX(lasOvejas.get(i).getX() + ((lasOvejas.get(i).getX() >= 1163) ? -velocidadOvejas : velocidadOvejas));
                lasOvejas.get(i).setY(lasOvejas.get(i).getY() + ((lasOvejas.get(i).getY() >= 27) ? -velocidadOvejas : velocidadOvejas));
            } else if (numerosAleatorios[randomX] == 1 && numerosAleatorios[randomY] == -1) {
                lasOvejas.get(i).setX(lasOvejas.get(i).getX() + ((lasOvejas.get(i).getX() >= 1163) ? -velocidadOvejas : velocidadOvejas));
                lasOvejas.get(i).setY(lasOvejas.get(i).getY() - ((lasOvejas.get(i).getY() >= 672) ? velocidadOvejas : -velocidadOvejas));
            } else if (numerosAleatorios[randomX] == -1 && numerosAleatorios[randomY] == -1) {
                lasOvejas.get(i).setX(lasOvejas.get(i).getX() - ((lasOvejas.get(i).getX() >= 5) ? velocidadOvejas : -velocidadOvejas));
                lasOvejas.get(i).setY(lasOvejas.get(i).getY() - ((lasOvejas.get(i).getY() >= 672) ? velocidadOvejas : -velocidadOvejas));
            } else if (numerosAleatorios[randomX] == -1 && numerosAleatorios[randomY] == 1) {
                lasOvejas.get(i).setX(lasOvejas.get(i).getX() - ((lasOvejas.get(i).getX() >= 5) ? velocidadOvejas : -velocidadOvejas));
                lasOvejas.get(i).setY(lasOvejas.get(i).getY() + ((lasOvejas.get(i).getY() >= 27) ? -velocidadOvejas : velocidadOvejas));
            }
        }
    }

    public void moverLobo() {
        for (int i = 0; i < losLobos.size(); i++) {
            int randomX;
            int randomY;

            int[] numerosAleatorios = {-1, 1}; // números aleatorios a seleccionar
            randomX = r.nextInt(2);
            randomY = r.nextInt(2);

            /*
            *Movimientos aleatorios comprobando los limites del frame
             */
            if (numerosAleatorios[randomX] == 1 && numerosAleatorios[randomY] == 1) {
                losLobos.get(i).setX(losLobos.get(i).getX() + ((losLobos.get(i).getX() >= 1163) ? -velocidadLobos : velocidadLobos));
                losLobos.get(i).setY(losLobos.get(i).getY() + ((losLobos.get(i).getY() >= 27) ? -velocidadLobos : velocidadLobos));
            } else if (numerosAleatorios[randomX] == 1 && numerosAleatorios[randomY] == -1) {
                losLobos.get(i).setX(losLobos.get(i).getX() + ((losLobos.get(i).getX() >= 1163) ? -velocidadLobos : velocidadLobos));
                losLobos.get(i).setY(losLobos.get(i).getY() - ((losLobos.get(i).getY() >= 672) ? velocidadLobos : -velocidadLobos));
            } else if (numerosAleatorios[randomX] == -1 && numerosAleatorios[randomY] == -1) {
                losLobos.get(i).setX(losLobos.get(i).getX() - ((losLobos.get(i).getX() >= 5) ? velocidadLobos : -velocidadLobos));
                losLobos.get(i).setY(losLobos.get(i).getY() - ((losLobos.get(i).getY() >= 672) ? velocidadLobos : -velocidadLobos));
            } else if (numerosAleatorios[randomX] == -1 && numerosAleatorios[randomY] == 1) {
                losLobos.get(i).setX(losLobos.get(i).getX() - ((losLobos.get(i).getX() >= 5) ? velocidadLobos : -velocidadLobos));
                losLobos.get(i).setY(losLobos.get(i).getY() + ((losLobos.get(i).getY() >= 27) ? -velocidadLobos : velocidadLobos));
            }
        }
    }

///////////////////////////////////////////////////////////////////////////// 
    //autor: jonathan loaiza rosero
    public int ovejaMasCercana(int wolf){
       
        int x1 = losLobos.get(wolf).getX();
        int y1 = losLobos.get(wolf).getY();
        int ovejaMasCercana = 0;                                         // se compara la distancia de un lobo con todas
        int distanciaOvejaMasCercana=2000;                               // las ovejas para saber cúal es la mas cercana
        for(int i = 0;i<lasOvejas.size();i++){
            int dist = distancia(x1,y1,lasOvejas.get(i).getX(),lasOvejas.get(i).getY());
            if(dist < distanciaOvejaMasCercana){
                distanciaOvejaMasCercana = dist;
                ovejaMasCercana = i; 
                }
        }
        return ovejaMasCercana;
    }
    
    public int distancia(int x1,int y1, int x2, int y2){
        int Dx = x2 - x1;
        int Dy = y2 - y1;                             //clasica formula de la distancia entre dos puntos. 
        return (int)Math.sqrt(Dx*Dx + Dy*Dy); 
    }
    
    public void moverLovoHastaOvejaObjetivo(){
        int x1 = losLobos.get(loboAleatorio).getX();
        int y1 = losLobos.get(loboAleatorio).getY();
        int x2 = lasOvejas.get(ovejaObjetivo).getX();
        int y2 = lasOvejas.get(ovejaObjetivo).getY();
        
        //Acontinuación se implementa el algoritmo incremental básico pero solo haciendo 3 iteraciones 
        //para que se presente un comportamiento "coerente" de el movimiento del lobo
        
  
	int y=y1;
        int z = 0;
        int dist = distancia(x1,y1,x2,y2);
        if(dist>=0 && dist<=20){
            lasOvejas.remove(ovejaObjetivo);
            horaDeComer = false;
            
        }else{
	if(x1!=x2){
            int m = (y2-y1)/(x2-x1);
              for(int x = x1;x<=x2;x++){
		if(z<=2){
                    y += m;
                    z+=1;                         //si, solo se mueve un lobo los demás quedan inmoviles hay que arreglarlo.
                }else{
                    x1=x;
                    x = x2+1;
                }
            }
            z=0;
            losLobos.get(loboAleatorio).setX(x1);
            losLobos.get(loboAleatorio).setY(y);
        }else{
            int x = x1;
            for(int j=y1;j<=y2;j++){
		if(z<=2){    //z<=2
                    z+=1;
                }else{
                    y1=j;
                    j = y2+1;
                }
            }
            z=0;
            losLobos.get(loboAleatorio).setX(x);
            losLobos.get(loboAleatorio).setY(y1);
}
        
    }
    }
////////////////////////////////////////////////////////////////////////////////    
    
    public void agregarPasto() {
        elPasto = laSimulacion.Pasto(cantPasto);
    }

}
