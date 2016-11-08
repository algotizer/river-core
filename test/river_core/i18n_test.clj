(ns river-core.i18n-test
  (:require
    [clojure.test :as t]
    [river-core.i18n :as i18n]))


(t/deftest lang-slug-should-skip-lang-part-from-filename
  (t/is (= "hello" (i18n/lang-slug "en.hello.md")))
  (t/is (= "hello" (i18n/lang-slug "zh_tw.hello.md")))
  (t/is (= "" (i18n/lang-slug "en.md")))
  (t/is (= "" (i18n/lang-slug ".md")))
  (t/is (= "" (i18n/lang-slug "hello.md"))))

(t/deftest prefix-lang-permalink-should-prefix-lang-to-path
  ; absolute path
  ;(t/is (= "/en/hello-world/" (i18n/prefix-lang-permalink {:parent-path "/" :slug "hello-world" :lang "en"})))
  (t/is (= "/en/some/path/hello-world/" (i18n/prefix-lang-permalink {:parent-path "some/path/" :slug "hello-world" :lang "en"})))
  (t/is (= "/zh-TW/hello-world/" (i18n/prefix-lang-permalink {:parent-path "" :lang "zh-TW" :slug "hello-world"})))
  ; should skip lang if not exists
  (t/is (= "/some/path/hello-world/" (i18n/prefix-lang-permalink {:parent-path "some/path" :slug "hello-world"}))))
