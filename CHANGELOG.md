# Changelog

## master (unreleased)

### New Features
    
### Changes
                    
### Bugs fixed 

    - Fix autoprefixes on Windows
    
## 1.12.0-4 (19-02-2025)

### New Features
    
### Changes
                    
### Bugs fixed 

    - Fix let/loop bindings not being traced inside reify

## 1.12.0-3 (28-01-2025)

### New Features
    
### Changes

    - Bring back instrumentation to the compile path to support decompiling for debugging
                    
### Bugs fixed 
    
## 1.12.0-2 (27-11-2024)

### New Features
    
### Changes
                    
### Bugs fixed 

    - Trace most statements also

## 1.12.0-1 (06-09-2024)

### New Features
    
### Changes
                    
### Bugs fixed 

    - Fix blank prefixes properties case

## 1.12.0 (06-09-2024)

### New Features
    
### Changes

    - Tracking official 1.12.0
                    
### Bugs fixed 

## 1.12.0-rc2 (02-09-2024)

### New Features
    
### Changes

    - Tracking official 1.12.0-rc2
                    
### Bugs fixed 

## 1.12.0-rc1 (03-08-2024)

### New Features
    
### Changes

    - Tracking official 1.12.0-rc1
                    
### Bugs fixed 

## 1.12.0-beta2 (02-08-2024)

### New Features
    
### Changes

    - Tracking official 1.12.0-beta2
                    
### Bugs fixed 

## 1.12.0-beta1_1 (20-06-2024)

### New Features
    
### Changes

    - Don't instrument in .class compilation path 
    - Lein init form instrumentation ignore
    - Move maybe-init-flow-storm out of clojure.main/repl to support nrepl >= 1.3.0
    - Move maybe-execute-storm-specials into Compiler.eval to support nrepl >= 1.3.0
    
### Bugs fixed 

## 1.12.0-beta1 (20-06-2024)

### New Features
    
### Changes 

    - Tracking official 1.12.0-beta1
    - Don't waste time tagging forms for uninstrumented namespaces
    
### Bugs fixed 

    - Fix form walking for PersistentTreeMap (sorted-map)

## 1.12.0-alpha12 (27-05-2024)

### New Features
    
### Changes 

    - Tracking official 1.12.0-alpha12
    
### Bugs fixed 

## 1.12.0-alpha11_1 (14-05-2024)

### New Features
    
### Changes 
    
### Bugs fixed

    - Don't tag ()
    - After eliding meta (reader and coord) leave nil instead of {} when that was the only meta
    - Refactor Utils.mergeMeta
    
## 1.12.0-alpha11 (unreleased)
	
### New Features
    
    - Instance methods instrumentation
    
### Changes 
    
    - Tracking official 1.12.0-alpha11
    
### Bugs fixed

## 1.12.0-alpha9_4 (06-04-2024)
	
### New Features

    - Emitter now exposes setInstrumentationSkipRegex, removeInstrumentationSkipRegex and getInstrumentationSkipRegex
    
### Changes 
    
### Bugs fixed
