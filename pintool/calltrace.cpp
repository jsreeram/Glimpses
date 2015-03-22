/*BEGIN_LEGAL
 Intel Open Source License

 Copyright (c) 2002-2008 Intel Corporation
 All rights reserved.
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are
 met:

 Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.  Redistributions
 in binary form must reproduce the above copyright notice, this list of
 conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution.  Neither the name of
 the Intel Corporation nor the names of its contributors may be used to
 endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE INTEL OR
 ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 END_LEGAL */

/* ===================================================================== */
/*
 @ORIGINAL_AUTHOR: Robert Muth
 @MODIFIED_BY: Sarang Ozarde
 */

/* ===================================================================== */
/*! @file
 *  This file contains an ISA-portable PIN tool for tracing instructions
 */

#include "pin.H"
#include <iostream>
#include <fstream>
#include <set>
#include <map>
#include <list>
#include <string.h>
#include <cstdio>
#include <cstdlib>

#define MAX_FUNCS 25000
#define MAX_LOADS 5500
#define TARGET_IA32 1

/* ===================================================================== */
/* Structures*/
/* ===================================================================== */
struct ltstr
{
	bool operator()(const char* s1, const char* s2) const
	{
		return strcmp(s1, s2) < 0;
	}
};

struct _Func
{
	int id;
	int nBranches;
	int nCodeSize;
	int nNoOfCalls;
	int memoryAlloc1;
	int loadmax;
	int loadmin;
	int avloops;
	string name;
	ADDRINT nStackSize;
	set<int> InstrExe;
	set<int> calledFuncs;
	list<ADDRINT> loads;
	
	_Func()
	{
		loadmax = -1;
		loadmin = -1;
	}
};

struct _BasicBlock;

struct _Instruction
{
	INS ins;
	_BasicBlock* pBasicBlock;
	bool bLeader;
	_Instruction()
	{
		pBasicBlock = NULL;
		//ins = NULL;
		bLeader = false;
	}
};

struct _BasicBlock
{
	ADDRINT startInsAddr;
	list<_Instruction*> listInstructions;
	_BasicBlock *left;
	_BasicBlock *right;

	_BasicBlock()
	{
		left = NULL;
		right = NULL;
	}
};

/* ===================================================================== */
/* Function Pointers */
/* ===================================================================== */


typedef VOID * (*FUNCPTR_MALLOC)(size_t);
typedef VOID (*FUNCPTR_FREE)(void *);
typedef VOID (*FUNCPTR_EXIT)(int);


/* ===================================================================== */
/* Global Variables */
/* ===================================================================== */

std::ofstream TraceFile;
string invalid = "invalid_rtn";

_Func Func[MAX_FUNCS]; // TODO: change to vector
_Func _DynamicFunc[MAX_FUNCS]; // TODO: change to vector
map<const char*, int, ltstr> Functions;
map<const char*, int, ltstr> FunctionsDy;
int _st_nFuncCounter = 0;
int _dy_nFuncCounter = 0;
int prevIndex = -1;
int prevIndexforMalloc = -1;
set<int> setBranchesExe;
static const unsigned int chunk_size = 16;
list<_BasicBlock*> listBasicBlocks;
list<_Instruction*> listInstruction;
list<_BasicBlock*> listBasicBlockswithBranchTargets;
list<ADDRINT> listBranchTargets;


/* ===================================================================== */
/* C ommandline Switches */
/* ===================================================================== */

KNOB<string> KnobOutputFile(KNOB_MODE_WRITEONCE, "pintool", "o", "calltrace. out", "specify trace file name");
KNOB<BOOL> KnobPrintArgs(KNOB_MODE_WRITEONCE, "pintool", "a", "0","printcall arguments ");
//KNOB<BOOL> KnobPrintArgs(KNOB_MODE_WRITEONCE, "pintool", "i", "0", "mark indirect calls ");

/* ===================================================================== */

INT32 Usage()
{
	cerr <<
	"This tool produces data used for Glimpses Project .\n"
	"Author: Sarang Ozarde"
	"\n";

	cerr << KNOB_BASE::StringKnobSummary();

	cerr << endl;

	return -1;
}

/* ========================================= ============================ */

string *Target2String(ADDRINT target)
{
	string name = RTN_FindNameByAddress(target);
	if (name == "")
		return &invalid;
	else
		return new string(name);
}

/* ========================================= ============================ */

string FormProperFunctionName(ADDRINT addr)
{
	char buffer[50];

	string name = RTN_FindNameByAddress(addr);
	string temp;
	int i = 0;
	
	if (name == "")
		name = invalid;
	else if(name == ".text" )
	{
		PIN_LockClient();
		sprintf(buffer,"0x%X", (unsigned int)RTN_Address(RTN_FindByAddress(addr)));
		PIN_UnlockClient();
		name = buffer;
	}
	else if(name == ".plt")
	{
		name = "Library_Call";
	}
	else if(name.find("_Z") == 0)
	{
		name.erase(0,2);
		
		while((atoi(&name[i])) != 0)
		{
			temp += name[i];
			i++;
		}
		name.erase(0,i);
		i = atoi(temp.c_str());
		name = name.substr(0, i);	
	}

	return name;
}

/* ========================================= ============================ */

int _static_GetFunctionIndex(string strFunctionName)
{
	int nFunctionIndex = -1;
	map<const char*, int, ltstr>::iterator it;

	it = Functions.find(strFunctionName.c_str());
    if(it == Functions.end())
    {
    	nFunctionIndex = _st_nFuncCounter;
		Functions.insert(pair<const char*,int>(strFunctionName.c_str(),_st_nFuncCounter++) );
		if(_st_nFuncCounter == MAX_FUNCS)
		{
			cout << endl << "Error: Exceeded maximum function count!" << endl;
			exit(0);
		}
		Func[nFunctionIndex].id = nFunctionIndex;
		Func[nFunctionIndex].name = strFunctionName;
		Func[nFunctionIndex].nBranches = 0;
		Func[nFunctionIndex].nCodeSize = 0;
		Func[nFunctionIndex].nNoOfCalls = 0;
		Func[nFunctionIndex].memoryAlloc1 = 0;
		Func[nFunctionIndex].avloops = 0;
    }
	else
		nFunctionIndex = it->second;

    return nFunctionIndex;
}

/* ========================================= ============================ */

int _dynamic_GetFunctionIndex(string strFunctionName)
{
	int nFunctionIndex = -1;
	map<const char*, int, ltstr>::iterator it;

	it = FunctionsDy.find(strFunctionName.c_str());
    if(it == FunctionsDy.end())
    {
    	nFunctionIndex = _dy_nFuncCounter;
    	FunctionsDy.insert(pair<const char*,int>(strFunctionName.c_str(),_dy_nFuncCounter++) );
		if(_dy_nFuncCounter == MAX_FUNCS)
		{
			cout << endl << "Error: Exceeded maximum Dynamic function count !" << endl;
			exit(0);
		}
		_DynamicFunc[nFunctionIndex].id = nFunctionIndex;
		_DynamicFunc[nFunctionIndex].name = strFunctionName;
		_DynamicFunc[nFunctionIndex].nBranches = 0;
		_DynamicFunc[nFunctionIndex].nCodeSize = 0;
		_DynamicFunc[nFunctionIndex].nNoOfCalls = 0;
		_DynamicFunc[nFunctionIndex].memoryAlloc1 = 0;
		_DynamicFunc[nFunctionIndex].nStackSize = 0;
		_DynamicFunc[nFunctionIndex].avloops = 0;
    }
	else
		nFunctionIndex = it->second;

    return nFunctionIndex;
}

/* ========================================= ============================ */

VOID do_procedurecall(ADDRINT target, BOOL taken, ADDRINT arg0)
{
	if( !taken ) return;

	int nSrcFuncIndex = -1;
	int nTgtIndex = -1;

	string strTarget = FormProperFunctionName(target);


	nSrcFuncIndex = _dynamic_GetFunctionIndex(FormProperFunctionName(arg0));
	nTgtIndex = _dynamic_GetFunctionIndex(strTarget);

	if(strTarget.find("malloc") != string::npos && prevIndexforMalloc == -1)
	{
		prevIndexforMalloc = nSrcFuncIndex;
	}

	if(strTarget == "Library_Call")
	{
		
		prevIndex = nSrcFuncIndex;
	}
	else
	{
		prevIndex = -1;
		_DynamicFunc[nSrcFuncIndex].calledFuncs.insert(nTgtIndex);
		_DynamicFunc[nTgtIndex].nNoOfCalls++;
	}

}

/* ===================================================================== */

VOID  do_call_plt(ADDRINT target, BOOL taken)
{
    if( !taken ) return;


    string strTarget = FormProperFunctionName(target);
	int nTgtIndex = _dynamic_GetFunctionIndex(strTarget);

	if(strTarget.find("malloc") != string::npos && prevIndexforMalloc == -1)
	{
		prevIndexforMalloc = prevIndex;
	}
	
	_DynamicFunc[prevIndex].calledFuncs.insert(nTgtIndex);
	_DynamicFunc[nTgtIndex].nNoOfCalls++;
}

/* ===================================================================== */

VOID CountBranchInstr(ADDRINT addr, BOOL taken)
{
	 //if( !taken ) return;
	
	pair<set<int>::iterator,bool> ret;
	
	ret = setBranchesExe.insert(addr);
	if(ret.second == true)
	{
		int nFuncIndex = _dynamic_GetFunctionIndex(FormProperFunctionName(addr));
		_DynamicFunc[nFuncIndex].nBranches++;
	}
}

/* ===================================================================== */

VOID CountInstr(ADDRINT addr, UINT32 nNumberofInstr)
{
	pair<set<int>::iterator,bool> ret;
	int nFuncIndex = _dynamic_GetFunctionIndex(FormProperFunctionName(addr));
	ret = _DynamicFunc[nFuncIndex].InstrExe.insert(addr);
	if(ret.second == true)
		_DynamicFunc[nFuncIndex].nCodeSize += nNumberofInstr;
		
	  	
}

/* ===================================================================== */

static ADDRINT mask(ADDRINT ea)  {
    const ADDRINT mask = ~static_cast<ADDRINT>(chunk_size-1);
    return ea & mask;
}

/* ===================================================================== */

static void load(ADDRINT addr, ADDRINT memea, UINT32 length) {
    ADDRINT start = mask(memea);
    ADDRINT end   = mask(memea+length-1);
    string strFunc = FormProperFunctionName(addr);
    int nFuncIndex = _dynamic_GetFunctionIndex(strFunc);
    //footprint_thread_data_t* tdata = xthis->get_tls(tid);
    for(ADDRINT addr = start ; addr <= end ; addr += chunk_size) {

    	//tdata->load(addr);
//    	if(_DynamicFunc[nFuncIndex].loadmax == -1)
//   	{
//    		_DynamicFunc[nFuncIndex].loadmax = addr;
//    		_DynamicFunc[nFuncIndex].loadmin = addr;
//    	}
//    	else
//    	{
//    		if(_DynamicFunc[nFuncIndex].loadmax < addr) _DynamicFunc[nFuncIndex].loadmax = addr;
//    		if(_DynamicFunc[nFuncIndex].loadmin > addr) _DynamicFunc[nFuncIndex].loadmin = addr;
//    	}
		//if(_DynamicFunc[nFuncIndex].loads.size() <= MAX_LOADS)
    		_DynamicFunc[nFuncIndex].loads.push_back(addr);
    }

}

/* ===================================================================== */

VOID Trace(TRACE trace, VOID *v)
{
	//const BOOL print_args = KnobPrintArgs.Value();
	for (BBL bbl = TRACE_BblHead(trace); BBL_Valid(bbl); bbl = BBL_Next(bbl))
	{
		INS tail = BBL_InsTail(bbl);
		if(INS_IsProcedureCall(tail))
		{
			//const ADDRINT target = INS_DirectBranchOrCal	lTarget Address(tail );
			//                    INS_InsertCall(tail, IPOINT_BEFORE, AFUNPTR(do_procedurecall),
			//                                 IARG_PTR, Target2String(target), IARG_PTR, Target2String(INS_Address(tail)), IARG_END);
			INS_InsertCall(tail, IPOINT_BEFORE, AFUNPTR(do_procedurecall),
					IARG_BRANCH_TARGET_ADDR, IARG_BRANCH_TAKEN, IARG_ADDRINT , INS_Address(tail), IARG_END);

		}
	   else
		{
			// sometimes code is not in an image
			RTN rtn = TRACE_Rtn(trace);

			// also track stup jumps into share libraries
			if( RTN_Valid(rtn) && !INS_IsDirectBranchOrCall(tail) && ".plt" == SEC_Name( RTN_Sec( rtn ) ))
			{
				//cout << "Inside plt my" << endl;
				INS_InsertCall(tail, IPOINT_BEFORE, AFUNPTR(do_call_plt),
						   IARG_BRANCH_TARGET_ADDR, IARG_BRANCH_TAKEN, IARG_END);
			}
		}
		INS head = BBL_InsHead(bbl);
		INS_InsertCall(head, IPOINT_BEFORE, AFUNPTR(CountInstr), IARG_INST_PTR, IARG_UINT32, BBL_NumIns(bbl), IARG_END);
		
	}
}

/* ===================================================================== */

_BasicBlock* CreateNewBB()
{
	_BasicBlock* pBB;
	pBB = new _BasicBlock();
	if(pBB == NULL)
	{
		cout << endl << "Out of Memory while building the basic blocks!" << endl;
		exit(0);
	}
	listBasicBlocks.push_back(pBB);
	return pBB;
}

/* ===================================================================== */

bool IsBranchTarget(_Instruction* pInstruction)
{
	list<ADDRINT>::iterator it;
	ADDRINT addr, tempAddr;

	addr = INS_Address((pInstruction->ins));
	for(it = listBranchTargets.begin(); it != listBranchTargets.end(); ++it)
	{
		tempAddr = (ADDRINT)*it;
		if(tempAddr == addr)
		{
			pInstruction->bLeader = true;
			return true;
		}
	}
	return false;
}

/* ===================================================================== */

_BasicBlock* GetBBStartingWithAddr(ADDRINT target)
{
	list<_BasicBlock*>::iterator itBB;
	_BasicBlock* pBasicBlock = NULL;
	for(itBB = listBasicBlockswithBranchTargets.begin(); itBB != listBasicBlockswithBranchTargets.end(); ++itBB )
	{
		pBasicBlock = (_BasicBlock*)(*itBB);
		if(pBasicBlock->startInsAddr == target)
			return pBasicBlock;
	}
	return NULL;
}

/* ===================================================================== */

void ClearBBDataSturcts()
{
	_BasicBlock* pBB;
	_Instruction *pInstr;

	listBasicBlockswithBranchTargets.clear();

	while(!listBasicBlocks.empty())
	{
		pBB = listBasicBlocks.front();
		listBasicBlocks.pop_front();
		delete pBB;
		pBB = NULL;
	}

	while(!listInstruction.empty())
	{
		pInstr = listInstruction.front();
		listInstruction.pop_front();
		delete pInstr;
		pInstr = NULL;
	}
}

/* ===================================================================== */

void BuildBasicBlocks(RTN rtn)
{

	// Find leaders
	list<_Instruction*>::iterator it;
	list<_Instruction*>::reverse_iterator itrev;
	_Instruction* pInstruction = NULL, *pPrevInstr = NULL;
	_BasicBlock* pBB = NULL, *pTempBB;
	bool bIsBranchTgt = false;

	list<_BasicBlock*>::iterator itBB;

	// Build the bbs and link fall throughs
	for(it = listInstruction.begin();it != listInstruction.end(); ++it)
	{
		pInstruction = (_Instruction*)(*it);
		bIsBranchTgt = IsBranchTarget(pInstruction);
		if(pInstruction->bLeader || bIsBranchTgt)
		{
			pTempBB = CreateNewBB();
			if(pPrevInstr && INS_HasFallThrough((pPrevInstr->ins)))
				pBB->right = pTempBB;

			pTempBB->startInsAddr = INS_Address((pInstruction->ins));

			if(bIsBranchTgt)
				listBasicBlockswithBranchTargets.push_back(pTempBB);

			pBB = pTempBB;
		}
		pBB->listInstructions.push_back(pInstruction);
		pPrevInstr = pInstruction;
	}

	//link the branches
	for(itBB = listBasicBlocks.begin(); itBB != listBasicBlocks.end(); ++itBB)
	{
		pBB = (_BasicBlock*)(*itBB);
		itrev = pBB->listInstructions.rbegin();
		if(itrev != pBB->listInstructions.rend())
		{
			pInstruction = (_Instruction*) (*itrev);
			if(INS_IsDirectBranchOrCall((pInstruction->ins)))
			{
				const ADDRINT target = INS_DirectBranchOrCallTargetAddress((pInstruction->ins));
				pTempBB = GetBBStartingWithAddr(target);
				if(pTempBB == NULL)
				{
					//cout << "Error occured while building basic blocks! Linking to next basic block can not be done!" << endl;
					//exit(0);
				}
				pBB->left = pTempBB;
			}
		}
	}
}

/* ===================================================================== */

void WrtiteBasicBlockInfo(string strRoutineName)
{
	list<_BasicBlock*>::iterator it;
	_BasicBlock* pBasicBlock = NULL;
	int nBBCount = 1;
	std::ofstream CfgFile;
	string strFileName = "./temp/CFGs/cfg."+strRoutineName+".dot";
	CfgFile.open (strFileName.c_str());

	cout << "Writing into file " << strFileName << endl;
	CfgFile << "digraph \"" << strRoutineName << "\" {" << endl;
	CfgFile << "label=\"CFG for " << strRoutineName << " function\"" << endl;



	for(it = listBasicBlocks.begin(); it != listBasicBlocks.end(); ++it, nBBCount++)
	{
		pBasicBlock = (_BasicBlock*)(*it);
		if(pBasicBlock->left && pBasicBlock->right)
		{
			CfgFile << "Node" << pBasicBlock << " [shape=record, label=" << "\"{Basic_Block" << nBBCount << ":|{<g0>T|<g1>F}}\"];" << endl;
			CfgFile << "Node" << pBasicBlock << ":g1 -> " << "Node" << pBasicBlock->left << ";" << endl;
			CfgFile << "Node" << pBasicBlock << ":g0 -> " << "Node" << pBasicBlock->right << ";" << endl;
		}
		else if(pBasicBlock->left)
		{
			CfgFile << "Node" << pBasicBlock << " [shape=record, label=" << "\"{Basic_Block" << nBBCount << ":|{<g0>}}\"];" << endl;
			CfgFile << "Node" << pBasicBlock << ":g0 -> " << "Node" << pBasicBlock->left << endl;
		}
		else if(pBasicBlock->right)
		{
			CfgFile << "Node" << pBasicBlock << " [shape=record, label=" << "\"{Basic_Block" << nBBCount << ":|{<g0>}}\"];" << endl;
			CfgFile << "Node" << pBasicBlock << ":g0 -> " << "Node" << pBasicBlock->right << ";" << endl;
		}
		else
		{
			CfgFile << "Node" << pBasicBlock << " [shape=record, label=" << "\"{Basic_Block" << nBBCount << ":}\"];" << endl;
		}
	}


	CfgFile << "}" << endl;


	CfgFile.close();
}

/* ===================================================================== */

_Instruction* CreateNewInstr()
{
	_Instruction* pInstr = NULL;
	pInstr = new _Instruction();
	if(pInstr == NULL)
	{
		cout << endl << "Out of Memory while building the basic blocks!" << endl;
		exit(0);
	}
	listInstruction.push_back(pInstr);
	return pInstr;
}

/* ===================================================================== */

struct _Image 
{
	string strImg;
	int nStaticFuncCount;
	int nDynamicFuncCount;
	
	_Image()
	{
		nStaticFuncCount = 0;
		nDynamicFuncCount = 0;
	}
};

map<const char*,_Image*, ltstr> mapImg;
map<const char*,int, ltstr> mapTempFunc;

VOID RtnStart(ADDRINT addr , ADDRINT stackptr)
{
	string strFunc = FormProperFunctionName(addr);
	string strImg;
	_Image *img = 0;
	map<const char*,_Image*, ltstr>::iterator itImg;
	map<const char*,int, ltstr>::iterator itFunc;
	itFunc = mapTempFunc.find(strFunc.c_str());
    if(itFunc == mapTempFunc.end())
    {
    	mapTempFunc.insert(pair<const char*,int>(strFunc.c_str(),0) );
    	PIN_LockClient();
    	strImg = IMG_Name(SEC_Img(RTN_Sec(RTN_FindByAddress(addr))));
    	PIN_UnlockClient();
    	itImg = mapImg.find(strImg.c_str());
    	if(itImg != mapImg.end())
    	{
    		img = (_Image*)itImg->second;
    		mapImg.erase(itImg);
    		img->nDynamicFuncCount++;
    		mapImg.insert(pair<const char*,_Image*>(strImg.c_str(),img));
    	}
    }
}

VOID CountAVLoops(ADDRINT addr)
{
	string strFunc = FormProperFunctionName(addr);
	int nFuncIndex = _dynamic_GetFunctionIndex(strFunc);
	_DynamicFunc[nFuncIndex].avloops++;
}

VOID StackSizeCount(ADDRINT addr , UINT32 size)
{
	string strFunc = FormProperFunctionName(addr);
	int nFuncIndex = _dynamic_GetFunctionIndex(strFunc);
	if(_DynamicFunc[nFuncIndex].nStackSize == 0)
		_DynamicFunc[nFuncIndex].nStackSize = size;
}

/* ===================================================================== */

VOID Routine(RTN rtn, VOID *v)
{

	string strInputPrgname = (char *)v;
	string strRoutineName;
	int nFuncIndex = -1;
	int nCalleeIndex = -1;
	int nBranchCount = 0;
	bool nStaticAnalFlag = true;
	bool bLeader = true;
	_Instruction* pInstr = NULL;
	int nStaticInstrCount = 0;
	_Image *img = 0;

	map<const char*, _Image*, ltstr>::iterator it;

	const string strImage = IMG_Name(SEC_Img(RTN_Sec(rtn)));
	
	it = mapImg.find(strImage.c_str());
    if(it == mapImg.end())
    {
//    	cout << "Image = " << strImage << endl;
		img = new _Image();
		if(img)
		{
			img->nStaticFuncCount = 1;
			img->strImg = strImage;
    		mapImg.insert(pair<const char*,_Image*>(strImage.c_str(),img) );
    	}
    	else
    	{
    		cout << "Out of memory while creating image hashtable entry!!!" << endl;
    		exit(0);
    	}
    }
    else
    {
    	img = (_Image*) it->second;
    	mapImg.erase(it);
    	img->nStaticFuncCount++;
    	mapImg.insert(pair<const char*,_Image*>(strImage.c_str(),img) );
    }
		
	//strRoutineName = FormProperFunctionName(RTN_Address(rtn));



	if(strImage.find(strInputPrgname) == string::npos)
	//if(strImage.find("/lib/ld-linux.so.2") == string::npos)
	{
//		it = Functions.find(strRoutineName.c_str());
//	    if(it == Functions.end())
	    	nStaticAnalFlag = false;
	}
	nStaticAnalFlag = true;
	if(nStaticAnalFlag)
	{
		strRoutineName = FormProperFunctionName(RTN_Address(rtn));
		nFuncIndex = _static_GetFunctionIndex(strRoutineName);
	}

	RTN_Open(rtn);

	INS ins = RTN_InsHead(rtn);
	INS temp = ins;

	// For each instruction of the routine
	for (; INS_Valid(ins); ins = INS_Next(ins))
	{

		nStaticInstrCount++;
		if(ins == temp)
		{
			//cout << "INS_Mnemonic = " << INS_Mnemonic(ins) << endl; 
			INS_InsertCall(ins, IPOINT_BEFORE, AFUNPTR(RtnStart), IARG_INST_PTR, IARG_PTR , strImage.c_str(), IARG_END);
		}
		//cout << "INS_Mnemonic = " << INS_Mnemonic(ins) << endl;

		if(nStaticAnalFlag)
		{

			if((INS_Mnemonic(ins) == "SUB")  && 
				(INS_OperandCount(ins) == 3) &&
				(INS_OperandIsReg(ins, 0 )))
				{
	  
					REG reg = INS_OperandReg(ins, 0);
					if((reg != REG_INVALID()) && (REG_StringShort (reg) == "esp") && (INS_OperandIsImmediate(ins,1)))
					{
						//cout << "INS_OperandImmediate(ins,1) " << INS_OperandImmediate(ins,1) << endl;   	
						if(Func[nFuncIndex].nStackSize == 0)
							Func[nFuncIndex].nStackSize = INS_OperandImmediate(ins,1);
						//INS_InsertCall(ins, IPOINT_ANYWHERE, AFUNPTR(StackSizeCount), IARG_INST_PTR, IARG_UINT32, INS_OperandImmediate(ins,1) , IARG_END);
					}
				}  

			// For building bbs

			pInstr = CreateNewInstr();
			pInstr->ins = ins;
			if(bLeader)
			{
				pInstr->bLeader = true;
				bLeader = false;
			}

			// For generating call graph
			if(INS_IsProcedureCall(ins))
			{
				if(INS_IsDirectBranchOrCall(ins) )
				{
					const ADDRINT target = INS_DirectBranchOrCallTargetAddress(ins);
					string targetstr = FormProperFunctionName(target);
					//if(targetstr != ".plt")
					//{
						nCalleeIndex = _static_GetFunctionIndex(targetstr);
						Func[nFuncIndex].calledFuncs.insert(nCalleeIndex);
						Func[nCalleeIndex].nNoOfCalls++;
					//}
				}
			}
			// For counting static branchs
			if(INS_IsBranchOrCall(ins))
			{
				nBranchCount++;

				// For building bbs
				if(INS_IsDirectBranchOrCall(ins))
				{
					const ADDRINT target = INS_DirectBranchOrCallTargetAddress(ins);
					RTN temprtn = RTN_FindByAddress(target);
					if(rtn == temprtn)
					{
						listBranchTargets.push_back(target);
						bLeader = true;
					}
				}
			}
			// counting auto vectorizable loops
			int i = 0;
			int nOperandCount = 0;
			nOperandCount = INS_OperandCount(ins);
			while(i < nOperandCount)
			{
				if(INS_OperandIsReg(ins, i ) )
				{
					REG reg = INS_OperandReg(ins, 0);
					if(reg != REG_INVALID())
					{
						if(REG_is_xmm(reg))
						{
							Func[nFuncIndex].avloops++;
						}  	
					}
					break;
				}
				i++;
			}
		}	

		// For counting dynamic branches
		if(INS_IsBranchOrCall(ins))
		{
			INS_InsertCall(ins, IPOINT_BEFORE, AFUNPTR(CountBranchInstr), IARG_INST_PTR, IARG_BRANCH_TAKEN, IARG_END);
		}
		
		
		if((INS_Mnemonic(ins) == "SUB")  && 
		(INS_OperandCount(ins) == 3) &&
		(INS_OperandIsReg(ins, 0 )))
		{
  			REG reg = INS_OperandReg(ins, 0);
			if((reg != REG_INVALID()) && (REG_StringShort (reg) == "esp") && (INS_OperandIsImmediate(ins,1)))
			{
		   	
				//if(Func[nFuncIndex].nStackSize == 0)
					//Func[nFuncIndex].nStackSize = INS_OperandImmediate(ins,1);
				INS_InsertCall(ins, IPOINT_BEFORE, AFUNPTR(StackSizeCount), IARG_INST_PTR, IARG_UINT32, (UINT32)INS_OperandImmediate(ins,1) , IARG_END);
			}
		} 

		// instrument the load(s)
		if (INS_IsMemoryRead(ins)) {
			INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR) load,
					IARG_INST_PTR,
					IARG_MEMORYREAD_EA,
					IARG_MEMORYREAD_SIZE,
					IARG_END);

		}

		// counting auto vectorizable loops
		int i = 0;
		int nOperandCount = 0;
		nOperandCount = INS_OperandCount(ins);
		while(i < nOperandCount)
		{
			if(INS_OperandIsReg(ins, i ) )
			{
				REG reg = INS_OperandReg(ins, 0);
				if(reg != REG_INVALID())
				{
					if(REG_is_xmm(reg))
					{
						//INS_InsertCall(ins, IPOINT_BEFORE, AFUNPTR(CountAVLoops), IARG_INST_PTR, IARG_END);
						//cout << "AVLOOP FOR= "<< strRoutineName << endl;
						int nFuncIndexAV = _dynamic_GetFunctionIndex(strRoutineName);
						_DynamicFunc[nFuncIndexAV].avloops++;
						
					}  	
				}
				break;
			}
			i++;
		}

		
	
		// For counting the number of instructions
		//if(ins == temp)
			//INS_InsertCall(ins, IPOINT_BEFORE, AFUNPTR(RtnStart), IARG_INST_PTR, IARG_END);
	}



	if(nStaticAnalFlag)
	{
		Func[nFuncIndex].nCodeSize = nStaticInstrCount;
		Func[nFuncIndex].nBranches = nBranchCount;
		BuildBasicBlocks(rtn);

		// Write the Control flow graph in file
		WrtiteBasicBlockInfo(strRoutineName);

		// Clear the lists
		ClearBBDataSturcts();
	}

	//INS temp = RTN_InsHead(rtn) ;

	RTN_Close(rtn);
}

/* ===================================================================== */

VOID * Jit_Malloc_IA32( CONTEXT * context, AFUNPTR orgFuncptr, size_t size)
{
    VOID * ret;

    PIN_CallApplicationFunction( context, PIN_ThreadId(),
                                 CALLINGSTD_DEFAULT, orgFuncptr,
                                 PIN_PARG(void *), &ret,
                                 PIN_PARG(size_t), size,
                                 PIN_PARG_END() );

    //cout << "PrevIndex= " << prevIndexforMalloc << " Size= " << size << endl;

    _DynamicFunc[prevIndexforMalloc].memoryAlloc1 +=  size;

    prevIndexforMalloc = -1;

    //TraceFile << "malloc(" << size << ") returns " << ret << endl;
    return ret;
}

/* ===================================================================== */

VOID Jit_Free_IA32( CONTEXT * context, AFUNPTR orgFuncptr, void * ptr)
{
    PIN_CallApplicationFunction( context, PIN_ThreadId(),
                                 CALLINGSTD_DEFAULT, orgFuncptr,
                                 PIN_PARG(void),
                                 PIN_PARG(void *), ptr,
                                 PIN_PARG_END() );

    //TraceFile << "free(" << ptr << ")" << endl;
}

/* ===================================================================== */


VOID * Jit_Malloc_IPF( FUNCPTR_MALLOC orgFuncptr, size_t size, ADDRINT appTP )
{
    // Get the tool thread pointer.
    ADDRINT toolTP = IPF_GetTP();

    //TraceFile << "In malloc, orgFuncptr = " << hex << (ADDRINT)orgFuncptr << dec << endl;
    IPF_SetTP( appTP );
    VOID * v = orgFuncptr(size);
    IPF_SetTP( toolTP );

    //cout << "PrevIndex= " << prevIndexforMalloc << " Size= " << size << endl;

    _DynamicFunc[prevIndexforMalloc].memoryAlloc1 += size;
    prevIndexforMalloc = -1;
    //TraceFile << "malloc(" << size << ") returns " << v << endl << flush;
    return v;
}

/* ===================================================================== */

VOID Jit_Free_IPF( FUNCPTR_FREE orgFuncptr, void * ptr, ADDRINT appTP )
{
    // Get the tool thread pointer.
    ADDRINT toolTP = IPF_GetTP();

    //TraceFile << "In free, orgFuncptr = " << hex << (ADDRINT)orgFuncptr << ", ptr = " << ptr << dec << endl;
    IPF_SetTP( appTP );

    orgFuncptr(ptr);
    IPF_SetTP( toolTP );
    //TraceFile << "free(" << ptr << ")"  << endl << flush;
}

/* ===================================================================== */

VOID Image(IMG img, VOID *v)
{
	const string strImage = IMG_Name(img);
    RTN mallocRtn = RTN_FindByName(img, "malloc");
    if (RTN_Valid(mallocRtn))
    {
        PROTO proto_malloc = PROTO_Allocate( PIN_PARG(void *), CALLINGSTD_DEFAULT,
                                             "malloc", PIN_PARG(size_t), PIN_PARG_END() );

#if defined ( TARGET_IA32 ) || defined ( TARGET_IA32E )

        RTN_ReplaceSignature(
            mallocRtn, AFUNPTR( Jit_Malloc_IA32 ),
            IARG_PROTOTYPE, proto_malloc,
            IARG_CONTEXT,
            IARG_ORIG_FUNCPTR,
            IARG_FUNCARG_ENTRYPOINT_VALUE, 0,
            IARG_END);
#else

        RTN_ReplaceSignature(
            mallocRtn, AFUNPTR( Jit_Malloc_IPF ),
            IARG_PROTOTYPE, proto_malloc,
            IARG_ORIG_FUNCPTR,
            IARG_FUNCARG_ENTRYPOINT_VALUE, 0,
            IARG_REG_VALUE, REG_TP,
            IARG_END);
#endif
        //TraceFile << "Replaced malloc() in:"  << IMG_Name(img) << endl;
    }

    RTN freeRtn = RTN_FindByName(img, "free");
    if (RTN_Valid(freeRtn))
    {
        PROTO proto_free = PROTO_Allocate( PIN_PARG(void), CALLINGSTD_DEFAULT,
                                           "free", PIN_PARG(void *), PIN_PARG_END() );

#if defined ( TARGET_IA32 ) || defined ( TARGET_IA32E )

        RTN_ReplaceSignature(
            freeRtn, AFUNPTR( Jit_Free_IA32 ),
            IARG_PROTOTYPE, proto_free,
            IARG_CONTEXT,
            IARG_ORIG_FUNCPTR,
            IARG_FUNCARG_ENTRYPOINT_VALUE, 0,
            IARG_END);
#else

        RTN_ReplaceSignature(
            freeRtn, AFUNPTR( Jit_Free_IPF ),
            IARG_PROTOTYPE, proto_free,
            IARG_ORIG_FUNCPTR,
            IARG_FUNCARG_ENTRYPOINT_VALUE, 0,
            IARG_REG_VALUE, REG_TP,
            IARG_END);
#endif
        //TraceFile << "Replaced free() in:"  << IMG_Name(img) << endl;
    }
}

/* ===================================================================== */

void WriteToXMLFile(string strFileName, int funccounter, _Func tempFunc[], bool bDynamic )
{

	//cout << "Writing Output to files" << endl;
	std::ofstream myfile;
	myfile.open (strFileName.c_str());
	myfile << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" << endl;
	myfile << "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">" << endl;
	myfile << "<graph edgedefault=\"directed\">" << endl;
	myfile << "<!-- data schema -->" << endl;
	myfile << "<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>" << endl;
	myfile << "<key id=\"codesize\" for=\"node\" attr.name=\"codesize\" attr.type=\"string\"/>" << endl;
	myfile << "<key id=\"stacksize\" for=\"node\" attr.name=\"stacksize\" attr.type=\"string\"/>"<< endl;
	myfile << "<key id=\"brfraction\" for=\"node\" attr.name=\"brfraction\" attr.type=\"string\"/>" << endl;
	myfile << "<key id=\"avloops\" for=\"node\" attr.name=\"avloops\" attr.type=\"string\"/>" << endl;
	myfile <<  "<key id=\"mallocsize\" for=\"node\" attr.name=\"mallocsize\" attr.type=\"string\"/>" << endl;
	myfile <<  "<key id=\"external\" for=\"node\" attr.name=\"external\" attr.type=\"int\"/>" << endl;
	myfile <<  "<key id=\"lslimithit\" for=\"node\" attr.name=\"lslimithit\" attr.type=\"string\"/>" << endl;
	myfile <<  "<key id=\"calls\" for=\"node\" attr.name=\"calls\" attr.type=\"int\"/>" << endl;
	myfile <<  "<key id=\"rank\" for=\"node\" attr.name=\"rank\" attr.type=\"string\"/>" << endl;
	myfile <<  "<!-- testfgmain-->\n";

	for(int i = 0; i < funccounter; i++)
	{

		if(bDynamic)
		{
			int nSamplesCnt = 0;
			string strFileName = "./temp/LoadsProfile/ld."+tempFunc[i].name;
			//FILE *file = fopen(strFileName.c_str(), "ab"); 
			ofstream profilefile ( strFileName.c_str(), ios_base::out | ios_base::app | ios_base::binary );
			
			int nTotalSamples = tempFunc[i].loads.size();
						
			int nModFactor = nTotalSamples/MAX_LOADS;
			
			if(nModFactor == 0) 
				nModFactor = 1;
			
			//profilefile << nModFactor << endl;
			list<ADDRINT>::iterator it;
			for(it = tempFunc[i].loads.begin(); it != tempFunc[i].loads.end() ; ++it, nSamplesCnt++)
			{
				//unsigned int addr = (*it);
				//fwrite(&addr, sizeof(int), 1, file);
				if(nSamplesCnt % nModFactor == 0)
					profilefile << (unsigned int)(*it) << endl;
				
			}
			//fclose(file);
			profilefile.close();

			int nStaticFuncIndex = _static_GetFunctionIndex(tempFunc[i].name);
			int nStaticCodeSize = Func[nStaticFuncIndex].nCodeSize;
			if(nStaticCodeSize != 0)
			{
				if(nStaticCodeSize < tempFunc[i].nCodeSize)
				{
					tempFunc[i].nCodeSize = nStaticCodeSize;	
				}						
			}
		}
		
		myfile << "<node id=\"" << tempFunc[i].id << "\">" << endl;
		myfile << "<data key=\"name\">" << tempFunc[i].name << "</data>" << endl;
		myfile << "<data key=\"codesize\">" << tempFunc[i].nCodeSize << "</data>" << endl;
		//myfile << "<data key=\"codefraction\">" <<110<< "</data>" << endl;
		myfile << "<data key=\"stacksize\">" << tempFunc[i].nStackSize << "</data>" << endl;
		myfile << "<data key=\"brfraction\">" << tempFunc[i].nBranches << "</data>" << endl;
		myfile << "<data key=\"avloops\">" << tempFunc[i].avloops << "</data>" << endl;
		myfile << "<data key=\"mallocsize\">" << tempFunc[i].memoryAlloc1 <<"</data>" << endl;
		myfile << "<data key=\"external\">0</data>" << endl;
		myfile << "<data key=\"lslimithit\">0</data>" << endl;
		myfile << "<data key=\"calls\">"<< tempFunc[i].nNoOfCalls << "</data>" << endl;
		myfile << "<data key=\"rank\">0</data>" << endl;
		myfile << "</node> " <<endl;

	}

	//(double)((double)(tempFunc[i].nBranches)/(double)(tempFunc[i].nCodeSize))
	for(int i = 0; i <=funccounter; i++)
	{
		set<int>::iterator it;
		for(it = tempFunc[i].calledFuncs .begin(); it != tempFunc[i].calledFuncs.end(); it++)
		{
			//if(tempFunc[i].id != *it)
			myfile << "<edge source=\"" << tempFunc[i].id << "\" target=\"" << *it << "\"></edge>" << endl;

		}
	}

	myfile << "</graph>" << endl;
	myfile << "</graphml>" << endl;

	myfile.close();
}


void WriteLibUtilFile()
{
	map<const char*,_Image*, ltstr>::iterator it;
	std::ofstream myfile;
	myfile.open("./temp/LibUtilRatio");
	_Image* img = 0;
	for(it = mapImg.begin(); it != mapImg.end(); ++it)
	{
		img = (_Image*)(it->second);
		if(img)
		{
			myfile << img->strImg << " " << img->nStaticFuncCount << " " << img->nDynamicFuncCount << " " << (((float)(img->nDynamicFuncCount)/(float)(img->nStaticFuncCount))*100) << endl;
		} 
	}
	
	myfile.close();
}

/* ===================================================================== */

VOID Fini(INT32 code, VOID *v)
{
	//TraceFile << "# eof" << endl;

	TraceFile.close();
	//loaddumpfile.close();
	WriteToXMLFile("./temp/StaticCG.xml",_st_nFuncCounter,Func, false);
	WriteToXMLFile("./temp/DynamicCG.xml",_dy_nFuncCounter,_DynamicFunc, true);
	WriteLibUtilFile();
}

/* ===================================================================== */

int main(int argc, char *argv[])
{

	PIN_InitSymbols();

	if( PIN_Init(argc,argv) )
	{
		return Usage();
	}

	//cout << "Input Program=" << argv[6] << endl;

//	loaddumpfile.open("./temp/parsed_loads.dmp");
//	loaddumpfile << dec;
//	loaddumpfile.setf(ios::showbase);
	//loaddumpfile << "172365856;3214500760;";

	TraceFile.open(KnobOutputFile.Value().c_str());

	TraceFile << hex;
	TraceFile.setf(ios::showbase);


	string trace_header = string("#\n"
			"# Call Trace Generated By Pin\n"
			"#\n");

	//TraceFile.write(trace_header.c_str(),trace_header.size());

	TRACE_AddInstrumentFunction(Trace, 0);
	RTN_AddInstrumentFunction(Routine,argv[6]);
	IMG_AddInstrumentFunction(Image, 0);

	PIN_AddFiniFunction(Fini, 0);

	// Never returns

	PIN_StartProgram();

	return 0;
}

/* ===================================================================== */
/* eof */
/* ===================================================================== */



