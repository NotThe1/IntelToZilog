package intelToZilog;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

public class InstructionSetIntel {

	public InstructionSetIntel() {
		// TODO Auto-generated constructor stub
	}//constructor

//	
//	public String getIntelInstruction() {
//		return intelInstruction;
//	}//getintelInstruction
//
	
	public static boolean isInstruction(String token) {
		return (instructions.containsKey(token));
	}// isInstruction

	public String getZilogInstruction(String intel) {
		return instructions.get(intel).zilogInstruction;
	}//getZilogInstruction
	
	public boolean isChangeNeeded(String intel) {
		return instructions.get(intel).changeNeeded;
	}//isChangeNeeded
	
	public ConversionType getConversionType(String intel) {
		return  instructions.get(intel).conversionType;
	}//getConversionType
	
	////////////////////////////////////////////////
	/* ----------------------------------------*/
	private  String getRegex() {
		StringBuilder sb = new StringBuilder("\\b(?i)(");
		Set<String> operations = getInstructionSet();
		for (String operation : operations) {
			sb.append(operation);
			sb.append("|");
		} // for
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")\\b");
//		System.out.printf("[InstructionSet.getRegex] sb: %s%n%n", sb.toString());
		;
		return sb.toString();
	}//getRegex
	
	public  Pattern getInstructionPattern() {
		return Pattern.compile(getRegex());
	}//
	
	
	public static Set<String> getInstructionSet(){
		 return instructions.keySet();
	}//getInstructionSet
	
	//---------------------------------------------------------
	private static HashMap<String, InstructionIntel> instructions;
	static {
		instructions = new HashMap<String, InstructionIntel>();

		instructions.put("NOP", new InstructionIntel("NOP",false,ConversionType.None));
		instructions.put("JP", new InstructionIntel("RET",false,ConversionType.None));
		instructions.put("DI", new InstructionIntel("DI",false,ConversionType.None));
		instructions.put("EI", new InstructionIntel("EI",false,ConversionType.None));
		instructions.put("DAA", new InstructionIntel("DAA",false,ConversionType.None));
		
		instructions.put("RLC", new InstructionIntel("RLCA",true,ConversionType.OnlyInstruction));
		instructions.put("RRC", new InstructionIntel("RRCA",true,ConversionType.OnlyInstruction));
		instructions.put("RAL", new InstructionIntel("RLA",true,ConversionType.OnlyInstruction));
		instructions.put("RAR", new InstructionIntel("RRA",true,ConversionType.OnlyInstruction));
		instructions.put("CMA", new InstructionIntel("CPL",true,ConversionType.OnlyInstruction));
		instructions.put("STC", new InstructionIntel("SCF",true,ConversionType.OnlyInstruction));
		instructions.put("CMC", new InstructionIntel("CCF",true,ConversionType.OnlyInstruction));
		instructions.put("HLT", new InstructionIntel("HALT",true,ConversionType.OnlyInstruction));
		
		instructions.put("CPI", new InstructionIntel("CP",true,ConversionType.OnlyInstruction));
		instructions.put("ORI", new InstructionIntel("OR",true,ConversionType.OnlyInstruction));
		instructions.put("XRI", new InstructionIntel("XOR",true,ConversionType.OnlyInstruction));
		instructions.put("ANI", new InstructionIntel("AND",true,ConversionType.OnlyInstruction));
		instructions.put("SUI", new InstructionIntel("SUB",true,ConversionType.OnlyInstruction));

		instructions.put("CALL", new InstructionIntel("CALL",true,ConversionType.OnlyInstruction));
		instructions.put("RET", new InstructionIntel("RET",true,ConversionType.OnlyInstruction));
		instructions.put("JMP", new InstructionIntel("JP",true,ConversionType.OnlyInstruction));


		instructions.put("INR", new InstructionIntel("INC",true,ConversionType.OnlyInstructionHL));
		instructions.put("DCR", new InstructionIntel("DEC",true,ConversionType.OnlyInstructionHL));
		instructions.put("MVI", new InstructionIntel("LD",true,ConversionType.OnlyInstructionHL));
		instructions.put("MOV", new InstructionIntel("LD",true,ConversionType.OnlyInstructionHL));
		
		instructions.put("SUB", new InstructionIntel("SUB",true,ConversionType.OnlyInstructionHL));
		instructions.put("ANA", new InstructionIntel("AND",true,ConversionType.OnlyInstructionHL));
		instructions.put("XRA", new InstructionIntel("XOR",true,ConversionType.OnlyInstructionHL));
		instructions.put("ORA", new InstructionIntel("OR",true,ConversionType.OnlyInstructionHL));
		instructions.put("CMP", new InstructionIntel("CP",true,ConversionType.OnlyInstructionHL));
		
		instructions.put("ADD", new InstructionIntel("ADD",true,ConversionType.LogicInstructionHL));
		instructions.put("ADC", new InstructionIntel("ADC",true,ConversionType.LogicInstructionHL));
		instructions.put("SBB", new InstructionIntel("SBC",true,ConversionType.LogicInstructionHL));
		
		
		instructions.put("LXI", new InstructionIntel("LD",true,ConversionType.Instruction16BitReg));
		instructions.put("INX", new InstructionIntel("INC",true,ConversionType.Instruction16BitReg));
		instructions.put("DCX", new InstructionIntel("DEC",true,ConversionType.Instruction16BitReg));
		instructions.put("PUSH", new InstructionIntel("PUSH",true,ConversionType.Instruction16BitReg));
		instructions.put("POP", new InstructionIntel("POP",true,ConversionType.Instruction16BitReg));
	
		instructions.put("RZ", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RNZ", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RC", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RNC", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RPO", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RPE", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RP", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		instructions.put("RM", new InstructionIntel("RET",true,ConversionType.ConditionInstruction));
		
		instructions.put("CZ", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CNZ", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CC", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CNC", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CPO", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CPE", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CP", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		instructions.put("CM", new InstructionIntel("CALL",true,ConversionType.ConditionInstruction));
		
		instructions.put("JZ", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JNZ", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JC", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JNC", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JPO", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JPE", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JP", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		instructions.put("JM", new InstructionIntel("JP",true,ConversionType.ConditionInstruction));
		
		instructions.put("STAX", new InstructionIntel("LD",true,ConversionType.STAX));
		instructions.put("LDAX", new InstructionIntel("LD",true,ConversionType.LDAX));

		instructions.put("DAD", new InstructionIntel("ADD",true,ConversionType.DAD));
		
		instructions.put("RST", new InstructionIntel("RST",true,ConversionType.RST));

		instructions.put("SBI", new InstructionIntel("SBC",true,ConversionType.SBI_ACI));
		instructions.put("ACI", new InstructionIntel("ADC",true,ConversionType.SBI_ACI));
		instructions.put("ADI", new InstructionIntel("ADD",true,ConversionType.SBI_ACI));
		
		//..................

		instructions.put("SHLD", new InstructionIntel("LD",true,ConversionType.SHLD));
		instructions.put("LHLD", new InstructionIntel("LD",true,ConversionType.LHLD));
		instructions.put("STA", new InstructionIntel("LD",true,ConversionType.STA));
		instructions.put("LDA", new InstructionIntel("LD",true,ConversionType.LDA));
		instructions.put("PCHL", new InstructionIntel("JP",true,ConversionType.PCHL));
		instructions.put("SPHL", new InstructionIntel("LD",true,ConversionType.SPHL));
		instructions.put("XCHG", new InstructionIntel("EX",true,ConversionType.XCHG));
		instructions.put("XTHL", new InstructionIntel("EX",true,ConversionType.XTHL));

		instructions.put("IN", new InstructionIntel("IN",true,ConversionType.IN));
		instructions.put("OUT", new InstructionIntel("OUT",true,ConversionType.OUT));

	}//static


}//class InstructionSetIntel 
