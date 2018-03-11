ifeq ($(OS),Windows_NT)
  $(error Windows not supported)
else
  detected_OS := $(shell uname -s)
  LIBNAME := libwrapper.so
  INCNAME := linux
  ifeq ($(detected_OS),Windows)
    $(error Windows not supported)
  endif
  ifeq ($(detected_OS),Darwin)
    LIBNAME := libwrapper.dylib
    INCNAME := darwin
  endif
endif
include config.mk

all: $(LIBNAME) SimpleServer.class PredictWrapper.class

SimpleServer.class: SimpleServer.java
	javac SimpleServer.java

PredictWrapper.class: PredictWrapper.java
	javac -d . ./JSON-java/*.java
	javac PredictWrapper.java

PredictWrapper.h: PredictWrapper.class
	javah PredictWrapper

$(LIBNAME): wrapper.c PredictWrapper.h model/model.c
	gcc -fPIC -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/$(INCNAME)" -I"$(PWD)/model" -shared -o $(LIBNAME) wrapper.c model/model.c

clean:
	rm -rfv org $(LIBNAME) PredictWrapper.h *.class
