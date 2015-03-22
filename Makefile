###########################################################################
#
# GLIMPSES toolkit
# Copyright (c) 2008,2009 Jaswanth Sreeram, Ashwini Bhagwat, Sarang Ozarde
# 
# Makefile: To build Glimpses
# 
# Note: Set the JAVA_HOME environment variable before running
#	this, since building prefuse will need it.
#
###########################################################################

TOP_LEVEL_DIR:=$(shell pwd)
PIN_DIR:=$(TOP_LEVEL_DIR)/pin
PIN_TOOL_DIR:=$(PIN_DIR)/source/tools/SimpleExamples
PATH:=$(PIN_DIR):$(PATH)
PREFUSE_DIR:=${TOP_LEVEL_DIR}/prefuse-beta
DATE:=$(shell date)


all::

#Build prefuse
	@echo "[GLIMPSES] *** Building Prefuse"
	@cd $(PREFUSE_DIR) && ./build.sh all && cd -

#Build PinTool
	@export COMPILER_PATH=/usr/bin
	@echo "[GLIMPSES] ***Bulding PinTool"
	@cd $(PIN_TOOL_DIR) && make

#Echo information to user
	@echo "[GLIMPSES] *** Glimpses Build Completed"
	@echo $(DATE)
	@chmod +x glimpses

clean:

	@echo "[GLIMPSES] *** Removing unnecessary Files"
	@cd $(PREFUSE_DIR) && ./build.sh clean && cd -
	@cd $(PIN_TOOL_DIR) && make clean
	@chmod -x glimpses

