# ClojureStorm

## Intro
Welcome to the ClojureStorm repository. ClojureStorm is a fork of the [official Clojure
compiler](https://github.com/clojure/clojure), with some extra code added to make it a dev compiler. 
This means a compiler with some extra capabilities tailored for development.

ClojureStorm will add instrumentation (extra bytecode to generated classes) to trace everything that is happening as your programs
execute. You use it by providing a bunch of callbacks that ClojureStorm will call as code runs.

## Starting a repl with ClojureStorm

```bash
clj -Sforce -Sdeps '{:deps {} :aliases {:dev {:classpath-overrides {org.clojure/clojure nil} :extra-deps {com.github.flow-storm/clojure {:mvn/version "RELEASE"}} :jvm-opts ["-Dclojure.storm.instrumentEnable=true" "-Dclojure.storm.instrumentOnlyPrefixes=dev"]}}}' -A:dev
```

The important bits here are :

- disable the official Clojure compiler
- add the ClojureStorm dependency
- tell ClojureStorm what namespaces to instrument via `instrumentOnlyPrefixes` in this case `dev`

## Hooking into ClojureStorm

Instructions here apply to ClojureStorm >= `1.11.1-19` and `1.12.0-alpha4_14`

```clojure
(clojure.storm.Tracer/setTraceFnsCallbacks
 {:trace-fn-call-fn (fn [_ fn-ns fn-name fn-args-vec form-id]
                      (prn "fn-call " fn-ns fn-name (into [] fn-args-vec) form-id))
  :trace-fn-return-fn (fn [_ ret coord form-id]
                        (prn "fn-return" ret coord form-id))
  :trace-fn-unwind-fn (fn [_ throwable coord form-id]
                        (prn "fn-unwind" throwable coord form-id))
  :trace-expr-fn (fn [_ val coord form-id]
                   (prn "expr" val coord form-id))
  :trace-bind-fn (fn [_ coord sym-name bind-val]
                   (prn "bind" coord sym-name bind-val))
  :handle-exception-fn (fn [thread ex] (println "Error"))})
```

Once that is set, you could try something like this :

```clojure
user=> (ns dev)
...
dev=> (defn sum [a b] (+ a b))
dev=> (sum 4 5)

"fn-call " "dev" "eval192" [] -1879070944
"fn-call " "dev" "sum" [4 5] -1340777963
"bind" "" "a" 4
"bind" "" "b" 5
"expr" 4 "3,1" -1340777963
"expr" 5 "3,2" -1340777963
"expr" 9 "3" -1340777963
"fn-return" 9 "" -1340777963
"expr" 9 "" -1879070944
"fn-return" 9 "" -1879070944

9
```

## Forms and coordinates

The example above  shows your callbacks receiving form ids and coordinates, let's see how you can use them.

ClojureStorm keeps a registry of all the forms it has compiled, which you can query like this :

```clojure
user=> (clojure.storm.FormRegistry/getForm -1340777963)

{:form/id -1340777963,
 :form/ns "dev",
 :form/form (defn sum [a b] (+ a b)),
 :form/def-kind :defn,
 :form/file "NO_SOURCE_PATH", :line 1}
```

Coords are strings with the coordinates inside the form tree.
In the case of our sum form, "2,1" means the third element (the `[a b]` vector), and then the first one `a`. 
Coordinates also work with unordered literals like sets, and maps with more than 8 keys.

If you want utility funcitons to work with forms and  coordinates take a look at
[get-form-at-coord](https://github.com/flow-storm/hansel/blob/master/src/hansel/utils.cljc#L74-L78) for example.

## Controlling instrumentation

Instrumentation can be controlled by 3 JVM properties :

  * `clojure.storm.instrumentOnlyPrefixes` a comma separated list of namespaces prefixes to instrument
  * `clojure.storm.instrumentSkipPrefixes` a comma separated list of namespaces prefixes to skip
  * `clojure.storm.instrumentSkipRegex` a regex to match namespaces to skip
  
which apply in that order.

You can add/remove instrumentation prefixes without restarting the repl by calling :

```clojure
(clojure.storm.Emitter/addInstrumentationOnlyPrefix "my-app")
(clojure.storm.Emitter/removeInstrumentationOnlyPrefix "my-app")
```
and check them by evaluating the `:help` key.

You can also disable/enable specific types of instrumentation by using :

```clojure
(clojure.storm.Emitter/setFnCallInstrumentationEnable true)
(clojure.storm.Emitter/setFnReturnInstrumentationEnable true)
(clojure.storm.Emitter/setExprInstrumentationEnable true)
(clojure.storm.Emitter/setBindInstrumentationEnable true)
```
## Applications using ClojureStorm

* [FlowStorm debugger](http://www.flow-storm.org)
* [Clofidence](https://github.com/flow-storm/clofidence)

## Clojure 

 *   Clojure
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (https://opensource.org/license/epl-1-0/)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.

## ASM

This program uses the ASM bytecode engineering library which is distributed
with the following notice:

 ASM: a very small and fast Java bytecode manipulation framework
 Copyright (c) 2000-2011 INRIA, France Telecom
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. Neither the name of the copyright holders nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 THE POSSIBILITY OF SUCH DAMAGE.

## Guava

This program uses the Guava Murmur3 hash implementation which is distributed
under the Apache License:


                                 Apache License
                           Version 2.0, January 2004
                        https://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS
