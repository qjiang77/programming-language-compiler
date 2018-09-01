package cop5556sp18;

import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
//Entry: scopeNum, name, Tyoe
//lookup: scopeNum???
//judge: scopeNum => name
//delete: delete scopeNum
import cop5556sp18.Types.Type;
import cop5556sp18.AST.Declaration;;

public class SymbolTable {
	class Entry{
		String name;
		Type type;
		Declaration declaration;
		Entry(String name, Type type,Declaration dec){
			this.name = name;
			this.type = type;
			this.declaration = dec;
		}
		public Type getType() {return this.type;}
		public String getName() {return this.name;}
	}
	Stack<Integer> scope_stack ;
	HashMap<Integer, ArrayList<Entry>> map ;
	int current_scope, next_scope;
	
	SymbolTable(){
		scope_stack = new Stack<Integer>();
		map = new HashMap<>();
		next_scope = 0;
		
	}
	void enterScope() {
		current_scope = next_scope++;
		//System.out.println("enter"+current_scope);
		scope_stack.push(current_scope);
	}
	
	void  leaveScope() {
		current_scope = scope_stack.pop();
		this.delete(current_scope);
		if(scope_stack.empty()){
			current_scope = 0;
		}else
			current_scope = scope_stack.peek();
		//System.out.println("leave"+current_scope);
	}
	
	public Type lookup(String name,int scopeNum) {
		//current_scope -> entry in map_entry
				//name -> type in entry
		System.out.println("lookup"+ scopeNum);
		for(int k = scopeNum; k >= 0; k--){
			System.out.println("xxxx"+k);
			
			if(map.containsKey(k)) {
				System.out.print("lookup"+ map.get(k).size());
				ArrayList<Entry> list = map.get(k);
				for(Entry e : list) {
					if(e.name.equals(name))
						return e.type;
				}
			}	
		}
		return Type.NONE;
	}
	public Declaration lookupDec(String name,int scopeNum) {
		//current_scope -> entry in map_entry
				//name -> type in entry
		System.out.println("lookup"+ scopeNum);
		for(int k = scopeNum; k >= 0; k--){
			System.out.println("xxxx"+k);
			
			if(map.containsKey(k)) {
				System.out.print("lookup"+ map.get(k).size());
				ArrayList<Entry> list = map.get(k);
				for(Entry e : list) {
					if(e.name.equals(name))
						return e.declaration;
				}
			}	
		}
		return null;
	}
	
	void add(String name, Declaration dec) {
		/*
		Entry<Integer, Entry> map_entry = new Entry<Integer, Entry>();
		Entry<String, Type> entry = new Entry<String, Type>();
		String attribute_name = entry.getKey();
		Type type = entry.getValue();
		scope_number = map_entry.getKey();
		entry = map_entry.getValue();
		*/
		
		if(!map.containsKey(current_scope)) {
			ArrayList<Entry> list = new ArrayList<Entry>();
			list.add(new Entry(name, Types.getType(dec.type),dec));
			map.put(current_scope, list);
			System.out.print(map.get(current_scope).size());
		}else {
			map.get(current_scope).add(new Entry(name, Types.getType(dec.type),dec));
			System.out.print(map.get(current_scope).size());
		}
		
		
		
		
	}
	public void delete(int current_scope) {
		if(map.containsKey(current_scope))
				map.remove(current_scope);
		
	}
	public boolean judge(String name) {
		// if current_scope -> entry
		// entry.key == name, return true;
		// else return false;
		if(!(map.containsKey(current_scope)))
				return false;
		ArrayList<Entry> list = map.get(current_scope);
		for(Entry e : list) {
			if(e.name.equals(name))
				return true;
		}
		return false;
	}
}



















