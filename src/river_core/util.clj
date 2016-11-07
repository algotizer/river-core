(ns river-core.util
  (:require
    [boot.core :as boot :refer [deftask]]
    [io.perun.core :as perun]
    [clojure.string :as string]
    [boot.pod :as pod]))

(def ^:private global-deps
  '[])

(defn- commit [fileset tmp]
  (-> fileset
      (boot/add-resource tmp)
      boot/commit!))

(defn- create-pod' [deps]
  (-> (boot/get-env)
      (update-in [:dependencies] into global-deps)
      (update-in [:dependencies] into deps)
      pod/make-pod))

(defn- create-pod
  [deps]
  (future (create-pod' deps)))

(defn- wrap-pool [pool]
  (let [prev (atom nil)]
    (fn [fileset]
      ; Do not refresh on the first run
      (let [pod (if (and @prev
                         (seq (->> fileset
                                   (boot/fileset-diff @prev)
                                   boot/input-files
                                   (boot/by-ext ["clj" "cljc"]))))
                  (pool :refresh)
                  (pool))]
        (reset! prev fileset)
        pod))))

(defn- assert-renderer [sym]
  (assert (and (symbol? sym) (namespace sym))
          "Renderer must be a fully qualified symbol, i.e. 'my.ns/fun"))

(defn- render-in-pod [pod sym render-data]
  (assert-renderer sym)
  (pod/with-eval-in pod
    (require '~(symbol (namespace sym)))
    ((resolve '~sym) ~(pod/send! render-data))))

(def ^:private +render-defaults+
  {:out-dir  "public"
   :filterer :content})

(deftask render
  "Render individual pages for entries in perun data.
   The symbol supplied as `renderer` should resolve to a function
   which will be called with a map containing the following keys:
    - `:meta`, global perun metadata
    - `:entries`, all entries
    - `:entry`, the entry to be rendered
   Entries can optionally be filtered by supplying a function
   to the `filterer` option.
   Filename is determined as follows:
   If permalink is set for the file, it is used as the filepath.
   If permalink ends in slash, index.html is used as filename.
   If permalink is not set, the original filename is used with file extension set to html."
  [o out-dir  OUTDIR   str  "the output directory (default: \"public\")"
   _ filterer FILTER   code "predicate to use for selecting entries (default: `:content`)"
   r renderer RENDERER sym  "page renderer (fully qualified symbol which resolves to a function)"]
  (let [pods    (wrap-pool (pod/pod-pool (boot/get-env)))
        tmp     (boot/tmp-dir!)
        options (merge +render-defaults+ *opts*)]
    (boot/with-pre-wrap fileset
      (let [pod   (pods fileset)
            files (filter (:filterer options) (perun/get-meta fileset))]
        (doseq [{:keys [path] :as file} files]
          (let [render-data   {:meta    (perun/get-global-meta fileset)
                               :entries (vec files)
                               :entry   file}
                html          (render-in-pod pod renderer render-data)
                page-filepath (perun/create-filepath
                                (:out-dir options)
                                ; If permalink ends in slash, append index.html as filename
                                (or (some-> (:permalink file)
                                            (string/replace #"/$" "/index.html")
                                            perun/url-to-path)
                                    (string/replace path #"(?i).[a-z]+$" ".html")))]
            (perun/report-debug "render" "rendered page for path" path)
            (perun/create-file tmp page-filepath html)))
        (perun/report-info "render" "rendered %s pages" (count files))
        (commit fileset tmp)))))
