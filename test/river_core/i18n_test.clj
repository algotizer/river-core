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
