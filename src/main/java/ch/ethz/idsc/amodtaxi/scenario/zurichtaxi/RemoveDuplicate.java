package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

public enum RemoveDuplicate {
    ;
    
    public static String spaces(String original){
        
        String reduced = original.replace("  ", " ");
        if(reduced.length() == original.length())
            return reduced;
        else return spaces(reduced);
        
    }
    
    
    public static void main(String [] args){
        
        System.out.println(spaces("a      b  cd    d   e"));
        
    }
    

}
