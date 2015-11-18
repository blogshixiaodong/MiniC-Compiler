package parser;

import java.util.ArrayList;
import java.util.Iterator;

public class AnalyzeTable {
	
	public static String error = "X";
	public static String acc = "acc";
	
	private DFA dfa;
	
	private String[] actionCol;
	private String[] gotoCol;
	public int actionLength;
	public int gotoLength;
	public int stateNum;
	
	private int[][] gotoTable;
	private String[][] actionTable;
	
	public AnalyzeTable(){
		createTableHeader();//����
		this.actionLength = actionCol.length;
		this.gotoLength = gotoCol.length;
		createDFA();//����DFA
		this.stateNum = dfa.size();
		this.gotoTable = new int[stateNum][gotoLength+actionLength-1];
		this.actionTable = new String[stateNum][actionLength];
		createAnalyzeTable();//����﷨���������������
	}
	
	/**
	 * ���������������һ��LR(1)�﷨�������ı�ͷ
	 */
	private void createTableHeader(){
		//�����ǽ���һ��������
		this.actionCol = new String[CFG.VT.size()+1];
		this.gotoCol = new String[CFG.VN.size()+CFG.VT.size()];
		Iterator<String> iter1 = CFG.VT.iterator();
		Iterator<String> iter2 = CFG.VN.iterator();
		int i = 0;
		int j = 0;
		while(iter1.hasNext()){
			String vt = iter1.next();
			if(!vt.equals(CFG.emp)){
				actionCol[i] = vt;
				gotoCol[i] = vt;
				i++;
			}
		}
		actionCol[i] = "$";
		while(iter2.hasNext()){
			String vn = iter2.next();
			gotoCol[i] = vn;
			i++;
		}
	}
	
	private ArrayList<DFAState> stateList = new ArrayList<DFAState>();//�������еݹ鷽����һ������������
	private ArrayList<Integer> gotoStart = new ArrayList<Integer>();
	private ArrayList<Integer> gotoEnd = new ArrayList<Integer>();
	private ArrayList<String> gotoPath = new ArrayList<String>();
	/**
	 * ��������ݹ鷽������һ�������﷨������DFA
	 * ��������ɲ�����ж���׼Ϊ:
	 */
	private void createDFA(){
		DFAState state0 = new DFAState(0);
		stateList.add(state0);
		stateList.get(0).addNewDerivation(new LRDerivation(getDerivation("S'").get(0),"$",0));//���ȼ���S'->��S,$
		for(int i = 0;i < stateList.get(0).set.size();i++){
			LRDerivation lrd = stateList.get(0).set.get(i);
			if(lrd.index < lrd.d.list.size()){
				String A = lrd.d.list.get(lrd.index);//��ȡ��������ķ�����
				String b = null;//����A��һ��+a
				if(lrd.index==lrd.d.list.size()-1){
					b = lrd.lr;
				} else {
					b = lrd.d.list.get(lrd.index+1);
				}
				if(CFG.VN.contains(A)){
					ArrayList<String> firstB = first(b);
					ArrayList<Derivation> dA = getDerivation(A);
					for(int j=0,length1=dA.size();j<length1;j++){
						for(int k=0,length2=firstB.size();k<length2;k++){
							LRDerivation lrd1 = new LRDerivation(dA.get(j),firstB.get(k),0);
							stateList.get(0).addNewDerivation(lrd1);
						}
					}
				}
			}
		}
		//state0�����ɹ���ʼ�ݹ齨��������״̬
		ArrayList<String> gotoPath = stateList.get(0).getGotoPath();
		for(String path:gotoPath){
			ArrayList<LRDerivation> list = stateList.get(0).getLRDs(path);//ֱ��ͨ��·��������һ��״̬�����
			addState(0,path,list);//��ʼ���еݹ飬�������ڷ�����DFA
		}
		this.dfa = new DFA(stateList);
	}
	
	/**
	 * ͨ������һ������һ��״̬��������LR����ʽ��list��ȡ��һ��״̬��
	 * �����״̬�Ѿ����ڣ������κβ����������ݹ飬�����״̬�����ڣ�������״̬���������еݹ�
	 * @param list
	 * @param lastState ��һ��״̬�ı��
	 */
	private void addState(int lastState,String path,ArrayList<LRDerivation> list){
//		System.out.println(lastState+" "+path+" "+list.toString());TODO
		DFAState temp = new DFAState(stateList.size());//��ʼ��
		for(LRDerivation lrd:list){//�������LR(1)����ʽ
			String next = null;
			if(lrd.index+1==lrd.d.list.size()){
				next = lrd.lr;
			} else {
				next = lrd.d.list.get(lrd.index+1);
			}
			ArrayList<String> first = first(next);
			for(String s:first){
				LRDerivation newLrd = new LRDerivation(lrd.d,s,lrd.index+1);
				temp.addNewDerivation(newLrd);
			}
		}
		if(!stateList.contains(temp)){
			stateList.add(temp);
			gotoStart.add(lastState);
			gotoEnd.add(temp.id);
			gotoPath.add(path);
			ArrayList<String> newPath = temp.getGotoPath();
			if(newPath.size()!=0){
				for(String p:newPath){
					ArrayList<LRDerivation> newList = temp.getLRDs(p);
					addState(temp.id,p,newList);//��һ���ݹ�
				}
			}
		}
	}
	
	/**�÷�������ͨ��
	 * ���ڻ�ȡ��һ���ķ�������صĲ���ʽ
	 * @param v
	 * @return
	 */
	public ArrayList<Derivation> getDerivation(String v){
		ArrayList<Derivation> result = new ArrayList<Derivation>();
		Iterator<Derivation> iter = CFG.F.iterator();
		while(iter.hasNext()){
			Derivation d = iter.next();
			if(d.left.equals(v)){
				result.add(d);
			}
		}
		return result;
	}
	
	/**����ͨ��
	 * ���ڻ�ȡһ���ķ����ŵ�first
	 * @param v
	 * @return
	 */
	private ArrayList<String> first(String v){
		ArrayList<String> result = new ArrayList<String>();
		if(v.equals("$")){
			result.add("$");
		} else {
			Iterator<String> iter = CFG.firstMap.get(v).iterator();
			while(iter.hasNext()){
				result.add(iter.next());
			}
		}
		return result;
	}
	
	/**
	 * ���������������﷨���������������
	 */
	private void createAnalyzeTable(){
		for(int i = 0;i < gotoTable.length;i++){
			for(int j = 0;j < gotoTable[0].length;j++){
				gotoTable[i][j] = -1;
			}
		}
		for(int i = 0;i < actionTable.length;i++){
			for(int j = 0;j < actionTable[0].length;j++){
				actionTable[i][j] = AnalyzeTable.error;
			}
		}
		//�����﷨��������goto����
		int gotoCount = this.gotoStart.size();
		for(int i = 0;i < gotoCount;i++){
			int start = gotoStart.get(i);
			int end = gotoEnd.get(i);
			String path = gotoPath.get(i);
			int pathIndex = gotoIndex(path);
			this.gotoTable[start][pathIndex] = end;
		}
		//�����﷨��������action����
		int stateCount = dfa.states.size();
		for(int i = 0;i < stateCount;i++){
			DFAState state = dfa.get(i);//��ȡdfa�ĵ���״̬
			for(LRDerivation lrd:state.set){//��ÿһ�����з���
				if(lrd.index == lrd.d.list.size()){
					if(!lrd.d.left.equals("S'")){
						int derivationIndex = derivationIndex(lrd.d);
						String value = "r"+derivationIndex;
						actionTable[i][actionIndex(lrd.lr)] = value;//��Ϊ��Լ
					} else {
						actionTable[i][actionIndex("$")] = AnalyzeTable.acc;//��Ϊ����
					}
				} else {
					String next = lrd.d.list.get(lrd.index);//��ȡ��������ķ�����
					if(CFG.VT.contains(next)){//������һ���ս����
						if(gotoTable[i][gotoIndex(next)] != -1){
							actionTable[i][actionIndex(next)] = "����"+gotoTable[i][gotoIndex(next)];
						}
					}
				}
			}
		}
	}
	
	private int gotoIndex(String s){//����goto�е�����
		for(int i = 0;i < gotoLength;i++){
			if(gotoCol[i].equals(s)){
				return i;
			}
		}
		return -1;
	}
	
	private int actionIndex(String s){//����action�е�����
		for(int i = 0;i < actionLength;i++){
			if(actionCol[i].equals(s)){
				return i;
			}
		}
		return -1;
	}
	
	private int derivationIndex(Derivation d){//�����ǵڼ�������ʽ
		int size = CFG.F.size();
		for(int i = 0;i < size;i++){
			if(CFG.F.get(i).equals(d)){
				return i;
			}
		}
		return -1;
	}
	
	public String ACTION(int stateIndex,String vt){
		int index = actionIndex(vt);
		return actionTable[stateIndex][index];
	}
	
	public int GOTO(int stateIndex,String vn){
		int index = gotoIndex(vn);
		return gotoTable[stateIndex][index];
	}
	
	/**
	 * ��ӡ�﷨������
	 */
	public void print(){
		String colLine = "\t";
		for(int i = 0;i < actionCol.length;i++){
			colLine += "\t";
			colLine += actionCol[i];
		}
		for(int j = 0;j < gotoCol.length;j++){
			colLine += "\t";
			colLine += gotoCol[j];
		}
		System.out.println(colLine);
		int index = 0;
		for(int i = 0;i < dfa.states.size();i++){
			String line = "\t"+i;
			while(index < actionCol.length){
				line += "\t";
				line += actionTable[index];
			}
			index = 0;
			while(index < gotoCol.length){
				line += "\t";
				line += gotoTable[index];
			}
			index = 0;
			line += "\t";
			System.out.println(line);
		}
	}
	
	public int getStateNum(){
		return dfa.states.size();
	}
	
	public static void main(String[] args){
		AnalyzeTable table = new AnalyzeTable();
		System.out.println(table.getStateNum());
		table.dfa.printAllStates();
	}

}