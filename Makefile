.PHONY: help deps install deploy test clean

SHELL       := /bin/bash
export PATH := bin:$(PATH)
export LEIN_SNAPSHOTS_IN_RELEASE := yes

version      = $(shell grep ^version version.properties |sed 's/.*=//')
verfile      = version.properties
distjar      = $(PWD)/bin/boot.jar
bootjar      = boot/boot/target/boot-$(version).jar
podjar       = boot/pod/target/pod-$(version).jar
aetherjar    = boot/aether/target/aether-$(version).jar
aetheruber   = aether.uber.jar
workerjar    = boot/worker/target/worker-$(version).jar
corejar      = boot/core/target/core-$(version).jar
basejar      = boot/base/target/base-$(version).jar
baseuber     = boot/base/target/base-$(version)-jar-with-dependencies.jar
alljars      = $(podjar) $(aetherjar) $(workerjar) $(corejar) $(baseuber) $(bootjar)
java_version = $(shell java -version 2>&1 | awk -F '"' '/version/ {print $$2}' |awk -F. '{print $$1 "." $$2}')

mkdirs:
	mkdir -p bin

bin/lein: mkdirs
	wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -O bin/lein
	chmod 755 bin/lein

bin/boot: mkdirs
	curl -fsSLo bin/boot https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh
	chmod 755 bin/boot

deps: bin/lein bin/boot
