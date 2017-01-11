(ns river-core.core
  "common tasks for building website"
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask]]
            [boot.util :as util]
            [boot.pod :as pod]
            [io.perun.core :as perun]
            [io.perun.meta :as pm]
            [river-core.util :as ru]))

(def ^:private +prev-next-defaults+
  {:filterer     identity
   :sortby (fn [file] (:date-published file))
   :comparator (fn [i1 i2] (compare i2 i1))})

(deftask prev-next
  "Adds :prev :next keys to files metadata."
  [c comparator        COMPARATOR     code "sort by comparator function"
   s sortby            SORTBY         code "sort entries by function"
   _ filterer          FILTER         code "predicate to use for selecting entries (default: `identity`)"]
  (boot/with-pre-wrap fileset
    (let [options       (merge +prev-next-defaults+ *opts*)
          files         (filter (:filterer options) (perun/get-meta fileset))
          sorted        (sort-by (:sortby options) (:comparator options) files)
          assoc-prevnext   #(assoc %1 :next %2 :prev %3)
          updated-files (map assoc-prevnext sorted (concat (rest sorted) [nil]) (concat [nil] sorted))]
      (perun/report-debug "prev-next"  "generated prev" (map :prev updated-files))
      (perun/report-info "prev-next" "added prev-next to %s files" (count updated-files))
      (perun/merge-meta fileset updated-files))))

(def ^:private +match-and-merge-defaults+
  {:target-filterer        identity
   :source-filterer (fn [entry] (= "team" (:type entry)))
   :match-fn (fn [target-entry source-entry]
               (if (= (:name source-entry) (:author target-entry))
                  source-entry
                  nil))
   :merge-fn (fn [target-entry source-entry] (assoc target-entry :author-info source-entry))})

(defn key=
  ([target-key]
   (key= target-key target-key))
  ([target-key src-key]
   (fn [t s]
     (if (= (target-key t) (src-key s))
       s
       nil))))

(defn set-of [options & kws]
  (let [optset (set options)]
    (fn [e]
      (optset (get-in e kws)))))

(deftask match-and-merge
  "Adds additional data from set of files to another set of files' metadata by matching result. "
  [_ source-filterer   SOURCEFILTER   code "predicate to use for selecting writer entries (default {:type 'team'})"
   _ target-filterer   TARGETFILTER   code "predicate to use for selecting entries (default: `identity`)"
   _ match-fn          MATCHFN        code "predicate to use for matching two entries"
   _ merge-fn          MERGEFN        code "function to merge sources to targets"]
  (boot/with-pre-wrap fileset
    (let [options       (merge +match-and-merge-defaults+ *opts*)
          sources       (filter (:source-filterer options) (perun/get-meta fileset))
          targets       (filter (:target-filterer options) (perun/get-meta fileset))
          updated       (mapv #(if-let [matched (filter (partial (:match-fn options) %) sources)]
                                  ((:merge-fn options) % matched)
                                  %) targets)]
      (perun/report-info "match-and-merge" "updated %s files" (count updated))
      (perun/merge-meta fileset updated))))

(deftask serverside-rendering
  [e enable ENABLE bool "skip serverside rendering"]
  (let [options    (merge {:enable false} *opts*)]
    (boot/with-pre-wrap fileset
      (let [global-meta (pm/get-global-meta fileset)
            new-global-meta (assoc global-meta :serverside-rendering (not (:enable options)))]
        (-> fileset
            (pm/set-global-meta new-global-meta))))))
