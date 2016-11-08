(ns river-core.i18n
  {:boot/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util :as util]
    [io.perun.core :as perun]
    [river-core.util :as ru]))

(defn- default-lang-fn [m]
  "Parses `lang` portion out of the filename in the format: lang-title.ext"
  (let [ls (clojure.string/split (:filename m) #"[-\.]")
        langs (first ls)
        lang-parts (clojure.string/split langs #"[_]")
        norm-langs (if (> (count lang-parts) 1)
                     [(first lang-parts) (clojure.string/upper-case (second lang-parts))]
                     lang-parts)
        lang (clojure.string/join "-" norm-langs)]
      (keyword lang)))

(deftask lang
  "Adds :lang key to files metadata. Lang is derived from files metadata."
  [s lang-fn LANGFN code "function to build Lang from files metadata"]
  (boot/with-pre-wrap fileset
    (let [lang-fn       (or lang-fn default-lang-fn)
          files         (perun/get-meta fileset)
          updated-files (map #(assoc % :lang (lang-fn %)) files)]
      (perun/report-debug "lang" "generated slugs" (map :lang updated-files))
      (perun/report-info "lang" "added langs to %s files" (count updated-files))
      (perun/set-meta fileset updated-files))))

(defn default-lang-page
  "a hiccup template for generating a redirect-page for default language"
  [{global :meta files :entries :as all}]
  (str
    "<!DOCTYPE html>"
    "<html>"
      "<head>"
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
        "<meta http-equiv=\"Refresh\" content=\"0; URL=/" (name (get-in global [:site-languages 0] :en)) "/\">"
        "<title>Redirect</title>"
      "</head>"
      "<body>"
        "<a href=\"/" (name (get-in global [:site-languages 0] :en)) "/\">" (name (get-in global [:site-languages 0] :en)) "</a>"
      "</body>"
    "</html>"))

(defn lang-slug
  "`lang` aware slug function"
  [filename]
  (->> (clojure.string/split filename #"[-\.]")
       (drop 1)
       drop-last
       (clojure.string/join "-")
       clojure.string/lower-case))

(defn add-end-slash [path]
  "add end slash to path if path not ends with /"
  (if (or (.endsWith path "/") (= path ""))
    path
    (str path "/")))

(defn prefix-lang-permalink
  "create a permalink aware of `:lang`, so all files with same lang would be
   rebase to /your-lang/<your file link>/index.html"
  [{:keys [lang parent-path slug]}]
  (perun/absolutize-url
    (str (add-end-slash (if lang (name lang) "")) (add-end-slash parent-path) (add-end-slash slug))))

(defn postfix-lang-permalink
  "create a permalink aware of `:lang`, so all files with same lang would be
   rebase to /<your file link>/<your-lang>/index.html"
  [m]
  (perun/absolutize-url
    (str (:parent-path m) (add-end-slash (:slug m)) (add-end-slash (name (:lang m))))))

(defn domain-lang-permalink
  "create a permalink ignore :lang, but it need to use with `lang` aware
   file saving task"
  [m]
  (perun/absolutize-url
    (str (:parent-path m) (add-end-slash (:slug m)))))
