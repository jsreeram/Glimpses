PREFIX=${PARSECDIR}/pkgs/apps/blackscholes/inst/${PARSECPLAT}

ifdef source
	ifeq "$(source)" "simd" 
		SRC = blackscholes.simd.c
	endif
else
	SRC	= blackscholes.c
endif

CSRC    = $(SRC)
TARGET  = blackscholes 
M4_BASE = .
MACROS  = c.m4.pthreads

ifdef version
  ifeq "$(version)" "parallel"
    M4_SRC    = blackscholes.m4.cpp
    CSRC      = $(M4_SRC)
    MT        = -DENABLE_THREADS
    CXXFLAGS += -pthread
  endif
else
  MT        = 
endif

# Default build Double precision version
NCO     = -DNCO=2

ifdef chk_err
ERR     = -DERR_CHK
endif

ifdef func
FUNC = -DFUNC_EXE
endif

ifdef single
NCO = -DNCO=4
endif


ifdef size
SZ = -DN=$(size)
else
SZ = -DN=960
endif

ifdef double
NCO = -DNCO=2
endif

CXXFLAGS += $(MT) $(SZ) $(NCO) $(FUNC) $(ERR) $(CSRC)

all: $(TARGET)

$(TARGET): clean $(CSRC)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) $(LIBS) -o $(TARGET)

$(M4_SRC): $(SRC)
	m4 $(M4_BASE)/$(MACROS) $< > $@

clean:
	rm -f $(TARGET) $(M4_SRC)

install:
	mkdir -p $(PREFIX)/bin
	cp -f $(TARGET) $(PREFIX)/bin/$(TARGET)

