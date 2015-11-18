package parser;

public class LRDerivation {
	
	public Derivation d;
	public String lr;
	public int index;
	
	public LRDerivation(Derivation d,String lr,int index){
		this.d = d;
		this.lr = lr;
		this.index = index;
	}
	
	public String toString(){
		String result = d.left+"->";
		int length = d.list.size();
		for(int i = 0;i < length;i++){
			result += " ";
			if(i == index){
				result += "��";
			}
			result += d.list.get(i);
		}
		if(index == length){
			result += "��";
		}
		result += " ,";
		result += lr;
		return result;
	}
	
	public boolean equals(LRDerivation lrd){
		if(d.equals(lrd.d)&&lr.equals(lrd.lr)&&index==lrd.index){
			return true;
		} else {
			return false;
		}
	}
	
	public void print(){
		System.out.println(this.toString());
	}

}
