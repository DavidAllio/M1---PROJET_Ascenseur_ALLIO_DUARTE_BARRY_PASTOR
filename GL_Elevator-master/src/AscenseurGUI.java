import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class AscenseurGUI {

	static JPanel[] externals_buttons = new JPanel[6];
	static ArrayList<JButton> ButtonsList = new ArrayList<>();

	//Classe interne utilisée pour dessiner l'assenceur et les étages
	public static class ElevatorVisualizationPanel extends JPanel {
		private int y = 250;
		private int floor_height =50;
		private Action action;
		private int door_l=2,door_r=2;
		private int animation=0;
		private boolean do_animate=false;
		private Timer timer;
		private int previous_floor = 0;

		ElevatorVisualizationPanel(Action action) {
			this.action=action;
			timer = new Timer(50, e -> {
				int floor_detected=-1;
				if(!do_animate) {
					floor_detected=floor_detected();

					//L'ascenseur n'envoie qu'un et un seul signal quand ses capteurs franchissent un étage
					if (floor_detected!=-1&&previous_floor!=floor_detected) {
						previous_floor=floor_detected;
						Action.output_text("[ASCENSEUR] Etage détecté", true);
						action.detected_floor(floor_detected);
						if(action.can_open_doors()) {
							do_animate = true;
						}
					}
					Point coord = action.moveElevator();
					y = coord.y;
				}else{
					door_animation();
				}
				repaint();
			});
			timer.start();
		}

		private void door_animation() {
			if(animation<=75) {
				if(animation<25) {
					if(animation==0){
						action.doors_openeded();
						Action.output_text("[ASCENSEUR] Ouverture des portes", true);
					}
					door_r++;
					door_l++;
				}

				if(animation>50){
					door_r--;
					door_l--;
				}
				animation++;
			}else{
				animation=0;
				do_animate=false;
				action.doors_closed();
				Action.output_text("[ASCENSEUR] Fermeture des portes", true);
			}
		}

		int floor_detected() {
			if (action.is_moving()) {
				if (y == 0)
					return 5;
				if (y == 50)
					return 4;
				if (y == 100)
					return 3;
				if (y == 150)
					return 2;
				if (y == 200)
					return 1;
				if (y == 250)
					return 0;
				else
					return -1;
			} else {
				return -1;
			}
		}



		@Override
		public Dimension getPreferredSize() {
			return new Dimension(220, 300);
		}

		@Override
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D elevator = (Graphics2D) g.create();
			for(int i=0;i<5;i++) {
				elevator.setColor(Color.ORANGE);
				elevator.fillRect(0, i* floor_height, 200, floor_height);
				elevator.setColor(Color.YELLOW);
				elevator.fillRect(2, (i* floor_height)+2, 196, floor_height -4);
				elevator.setColor(Color.BLACK);
				elevator.drawString("[" + (5-i) + "]",180,(i* floor_height)+(floor_height /2));
			}
			elevator.setColor(Color.ORANGE);
			elevator.fillRect(0, 5* floor_height, 200, floor_height);
			elevator.setColor(Color.YELLOW);
			elevator.fillRect(2, (5* floor_height)+2, 196, floor_height -4);
			elevator.setColor(Color.BLACK);
			elevator.drawString("[RDC]",163,(5* floor_height)+(floor_height /2));

			elevator.setColor(Color.BLACK);
			elevator.fillRect(0,y, 80, floor_height);

			elevator.setColor(Color.LIGHT_GRAY);

			elevator.fillRect(0,y,(floor_height -10)-door_l, floor_height);
			elevator.fillRect(floor_height -10+door_r,y,(floor_height -10)-door_r, floor_height);
			elevator.dispose();
		}

	}

	private ArrayList<JButton> createFloorButtons(Action action){
		JButton Floor_button;
		Image button_icon;

		for(int i=5;i>=0;i--) {
			if(i!=0)
				Floor_button = new JButton("Etage "+i);
			else
				Floor_button = new JButton("   RDC  ");
			try {
				button_icon = ImageIO.read(getClass().getResource("etage"+i+"on.png"));
				Floor_button.setIcon(new ImageIcon(button_icon));
				button_icon = ImageIO.read(getClass().getResource("etage"+i+"off.png"));
				Floor_button.setPressedIcon(new ImageIcon(button_icon));
				Floor_button.setMaximumSize(new Dimension(194,60));
				final int finalI = i;
				Floor_button.addActionListener(e -> {
					action.get_Instructions().add_internal(finalI);
					action.print_instructions();
				});
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			ButtonsList.add(Floor_button);
		}
		Floor_button = new JButton();

		try {
			button_icon = ImageIO.read(getClass().getResource("boutonsAUoff.png"));
			Floor_button.setIcon(new ImageIcon(button_icon));
			button_icon = ImageIO.read(getClass().getResource("boutonsAUon.png"));
			Floor_button.setPressedIcon(new ImageIcon(button_icon));
			Floor_button.addActionListener(e -> {action.stop_all();});
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		ButtonsList.add(Floor_button);
		return ButtonsList;
	}

	private AscenseurGUI(Action action){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame f=new JFrame();//creating instance of JFrame

		GridBagLayout grid = new GridBagLayout();
		f.getContentPane().setLayout(grid);

		GridBagConstraints gbc = new GridBagConstraints();
		ElevatorVisualizationPanel EVP = new ElevatorVisualizationPanel(action);


		JPanel Panel_InternalCommand=new JPanel();
		Panel_InternalCommand.setLayout(new BoxLayout(Panel_InternalCommand, BoxLayout.Y_AXIS));
		JLabel Label_InternalCommand=new JLabel("Commandes internes");
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.anchor = GridBagConstraints.WEST;
		Panel_InternalCommand.add(Label_InternalCommand);
		for(JButton Floor_button:createFloorButtons(action)) {
			Panel_InternalCommand.add(Floor_button);
		}
		f.add(Panel_InternalCommand,gbc);

		JPanel Panel_Visualization=new JPanel();
		Panel_Visualization.setLayout(new BoxLayout(Panel_Visualization, BoxLayout.Y_AXIS));
		JLabel Label_Visualization=new JLabel("Visualisation");
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 100, 10, 10);
		Panel_Visualization.add(Label_Visualization);
		Panel_Visualization.add(EVP);
		f.add(Panel_Visualization,gbc);

		JPanel row,column;
		gbc.gridx = 2;
		gbc.gridy = 0;
		column=new JPanel();
		column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
		column.add(new JLabel("Appel de la cabine"));
		for(int i=5;i>=0;i--){
			row = new JPanel();
			//Declaration et initialisation des boutons qui serviront aux appels dans les étages
			row.add(new JLabel(" Etage "+i+" "));
			if(i!=5) {
				JButton jb = new JButton("/\\");
				final int finalI = i;
				jb.addActionListener(e -> {
					action.get_Instructions().add_external(finalI, Instructions.Sens.HAUT);
					externals_buttons[finalI].setBackground(Color.ORANGE);
					action.print_instructions();
				});
				row.add(jb);
			}
			if(i!=0) {
				JButton jb2 = new JButton("\\/");
				final int finalI = i;
				jb2.addActionListener(e -> {
					action.get_Instructions().add_external(finalI, Instructions.Sens.BAS);
					externals_buttons[finalI].setBackground(Color.ORANGE);
					action.print_instructions();
				});
				row.add(jb2);
			}
			externals_buttons[i]=row;
			column.add(row);
		}
		f.add(column,gbc);

		JPanel Panel_OperativeControl=new JPanel();
		JPanel column1=new JPanel();
		JPanel column2=new JPanel();
		Panel_OperativeControl.setLayout(new BoxLayout(Panel_OperativeControl, BoxLayout.Y_AXIS));
		JLabel Label_OperativeControl=new JLabel("Contrôle du moteur");
		gbc.gridx = 0;
		gbc.gridy = 1;
		Panel_OperativeControl.add(Label_OperativeControl);
		JButton Up=new JButton("Monter");
		Up.addActionListener(e -> action.go_upstair());
		JButton Down=new JButton("Descendre");
		Down.addActionListener(e -> action.go_downstair());
		column1.add(Up);
		column1.add(Down);
		Panel_OperativeControl.add(column1);
		JButton StopNextFloor=new JButton("Arrêter au prochain niveau");
		StopNextFloor.addActionListener(e -> action.next_floor());
		JButton Stop=new JButton("ARRET D'URGENCE");
		Stop.addActionListener(e -> action.stop_all());
		column2.add(StopNextFloor);
		column2.add(Stop);
		Panel_OperativeControl.add(column1);
		Panel_OperativeControl.add(column2);
		f.add(Panel_OperativeControl,gbc);

		JPanel Panel_Output=new JPanel();
		Panel_Output.setLayout(new BoxLayout(Panel_Output, BoxLayout.Y_AXIS));
		JLabel Label_Output=new JLabel("Output :");
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		action.get_text_area().setEditable(false);
		JScrollPane scrollableTextArea = new JScrollPane(action.get_text_area());
		scrollableTextArea.setPreferredSize(new Dimension(500,200));
		scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Panel_Output.add(Label_Output);
		Panel_Output.add(scrollableTextArea);
		f.add(Panel_Output,gbc);

		f.setTitle("Projet Ascenseur");
		f.setSize(1120,650);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		f.setVisible(true);//making the frame visible
	}
	public static void main(String[] args) {
		Instructions ins = new Instructions();
		Action action = new Action(ins);
		AscenseurGUI AscGUI = new AscenseurGUI(action);
	}
}


