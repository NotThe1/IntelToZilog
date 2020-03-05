package intelToZilog;
/* @formatter:off */

public enum ConversionType {
	None,
	OnlyInstruction,
	OnlyInstructionHL,
	Instruction16BitReg,
	LogicInstructionHL,
	ConditionInstruction,
	STAX,
	LDAX,
	DAD,
	RST,
	SBI_ACI,
	
	SHLD,
	LHLD,
	STA,
	LDA,
	PCHL,
	SPHL,
	XCHG,
	XTHL,
	IN,
	OUT
}//enum ConversionType
//,SingleToDouble1,SingleToDouble2
/* @formatter:on  */
