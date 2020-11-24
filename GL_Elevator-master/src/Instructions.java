import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class Instructions {

	Action act;

	private ArrayList<Command> up_instructions_list = new ArrayList<>();
	private ArrayList<Command> down_instructions_list = new ArrayList<>();
	private ArrayList<Command> instructions_list= new ArrayList<>();

	Sens actual_direction = Sens.HAUT;
	private int actual_floor = 0;
	boolean actual_instruction_executed=true;
	boolean actual_instruction2_executed=true;
	int stop_to_floor=-1;

	boolean highest_floor=false;
	boolean lowest_floor=false;
	boolean d_closed=true;
	boolean emergency=false;
	Timer timer;

	public void set_Actionner(Action action) {
		act=action;
	}

	public enum Sens{HAUT,BAS};
	class Command  {
		public Integer floor;

		public Command(int floor){
			this.floor=floor;
		}
	}

	class CommandCompararatorUp implements Comparator<Command>{
		@Override
		public int compare(Command c1, Command c2)
		{

			return  c1.floor.compareTo(c2.floor);
		}
	}

	class CommandCompararatorDown implements Comparator<Command>{
		@Override
		public int compare(Command c1, Command c2)
		{

			return  c2.floor.compareTo(c1.floor);
		}
	}

	public void closed_doors(boolean bool) {
		d_closed=bool;
	}

	public ArrayList<Command> get_instructions(){
		return instructions_list;
	}

	public int get_floor(){
		return actual_floor;
	}

	public void upper_limits_reached(){
		highest_floor=true;
	}
	public void lowest_limits_reached(){
		lowest_floor=true;
	}
	public boolean is_go_up(){return actual_direction==Sens.HAUT;}
	public boolean is_go_down(){return actual_direction==Sens.BAS;}
	public void limits_waived(){
		highest_floor=false;
		lowest_floor=false;
	}

	public void emergency_stop() {
		instructions_list.removeAll(instructions_list);
		up_instructions_list.removeAll(up_instructions_list);
		down_instructions_list.removeAll(down_instructions_list);
		emergency=true;
		for(int i=0;i<6;i++)
			AscenseurGUI.externals_buttons[i].setBackground(UIManager.getColor ( "Panel.background" ));
	}

	public void emergency_start() {
		emergency=false;
	}

	public void direction_reversal(){
		if(actual_direction==Sens.HAUT){
			actual_direction=Sens.BAS;
		}else{
			actual_direction=Sens.HAUT;
		}

	}

	public void update_floor_level(int f) {
		actual_floor=f;
		Action.output_text("        [INSTRUCTION] Etage actuel : "+actual_floor,true);
			if (highest_floor)
				actual_direction=Sens.BAS;
			if(lowest_floor)
				actual_direction=Sens.HAUT;

	}

	void add_external(int floor, Sens s){
		if(emergency)
			return;
		if(floor==actual_floor)
			return;
		for(int i = 0; i< up_instructions_list.size(); i++){
			if(up_instructions_list.get(i).floor==floor)
				return;
		}
		for(int i = 0; i< down_instructions_list.size(); i++){
			if(down_instructions_list.get(i).floor==floor)
				return;
		}

		if(s==Sens.HAUT)
			up_instructions_list.add(new Command(floor));
		else
			down_instructions_list.add(new Command(floor));
	}

	//On rajoute l'étage de destination à la liste montante ou descendante de façon à ne pas géner la progression actuelle 
	public void add_internal(int floor) {
			int max=0,min=0;

			for(int i=0;i<up_instructions_list.size();i++){
				if(up_instructions_list.get(i).floor<min)
					min=up_instructions_list.get(i).floor;
			}

			if(floor>min) {
				add_external(floor, Sens.HAUT);
				return;
			}

			for(int i=0;i<down_instructions_list.size();i++){
				if(down_instructions_list.get(i).floor>max)
					max=down_instructions_list.get(i).floor;
			}

			if(floor<max) {
				add_external(floor, Sens.BAS);
				return;
			}
	}


	/*

	 */
	private void displacement_management(){

		if(is_go_up()) {
			if (up_instructions_list.size() > 0) {
				instructions_list = up_instructions_list;
			} else {
				if(down_instructions_list.size()>0) {
					instructions_list = down_instructions_list;
					direction_reversal();
				}
			}
		}else{
			if(down_instructions_list.size() > 0){
				instructions_list = down_instructions_list;
			}else{
				if(up_instructions_list.size()>0) {
					instructions_list = up_instructions_list;
					direction_reversal();
				}
			}
		}


		if(up_instructions_list.size()>1){
				Collections.sort(instructions_list, new CommandCompararatorUp());
		}else {
			if(down_instructions_list.size()>1) {
				Collections.sort(instructions_list, new CommandCompararatorDown());
			}
		}

	}

	private void displacement_executor(){
		if(instructions_list!=null&&instructions_list.size()>0) {

			if (stop_to_floor != instructions_list.get(0).floor) {
				actual_instruction_executed=false;
				actual_instruction2_executed=false;
				stop_to_floor = instructions_list.get(0).floor;
				Action.output_text("        [INSTRUCTION] Prochain étage à atteindre : "+stop_to_floor,true);

				if (stop_to_floor > actual_floor) {
					act.go_upstair();
				}else {
					act.go_downstair();
				}
			}

			if(!actual_instruction2_executed && ((actual_floor-stop_to_floor==1)||(actual_floor-stop_to_floor==-1))) {
				Action.output_text("        [INSTRUCTION] Appel à l'arrêt au prochain étage",true);
				act.next_floor();
				actual_instruction2_executed = true;
			}

			if (!actual_instruction_executed && (actual_floor == stop_to_floor)) {
				actual_instruction_executed = true;
				instructions_list.remove(0);
				AscenseurGUI.externals_buttons[stop_to_floor].setBackground(UIManager.getColor ( "Panel.background" ));
				Action.output_text("        [INSTRUCTION] Etage atteint, supression de cet étage dans la liste d'attente",true);
				return;
			}

		}
	}

	Instructions(){
		timer = new Timer(100, e -> {
				if(d_closed) {
					displacement_management();
					displacement_executor();
				}
		});
		timer.start();

	}

}
