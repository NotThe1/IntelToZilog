package intelToZilog;

/*
 *  this is basically a structure
 */

public class InstructionIntel {

	public String zilogInstruction;
	public boolean changeNeeded;
	public ConversionType conversionType;

	public InstructionIntel( String zilogInstruction, boolean changeNeeded,
			ConversionType conversionType) {

		this.zilogInstruction = zilogInstruction;
		this.changeNeeded = changeNeeded;
		this.conversionType = conversionType;
	}// Constructor
//	
//	public String getIntelInstruction() {
//		return intelInstruction;
//	}//getintelInstruction
//
//	public String getZilogInstruction() {
//		return zilogInstruction;
//	}//getZilogInstruction
//	
//	public boolean isChangeNeeded() {
//		return changeNeeded;
//	}//isChangeNeeded
//	
//	public ConversionType getConversionType() {
//		return  conversionType;
//	}//getConversionType

}// class InstructionIntel
