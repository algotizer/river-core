(set-env! :resource-paths #{"src"}
          :dependencies   '[[boot/core "2.5.1"]
                            [perun "0.3.0"]
                            [adzerk/bootlaces "0.1.13" :scope "test"]
                            [adzerk/boot-test "1.1.2" :scope "test"]])

(require '[adzerk.bootlaces :refer [bootlaces!
                                    build-jar push-snapshot push-release]]
         '[adzerk.boot-test :refer :all])

(def project 'river-core)
(def +version+ "0.1.0-SNAPSHOT")
(bootlaces! +version+)

(task-options!
  pom {:project     project
       :version     +version+
       :description "A collection of Boot tasks to support Perun"
       :url         "http://www.algotizer.com/"
       :scm         {:url "https://github.com/zerg000000/river-core"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}
  push {:ensure-branch  flase
        :ensure-clean   false
        :ensure-version +version+}})

(set-env! :source-paths #{"test"})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(deftask release-snapshot
  "Build and deploy to clojars"
  []
  (comp (build-jar)
        (push-snapshot)))
