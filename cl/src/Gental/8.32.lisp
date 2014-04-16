(defun SUM-NUMERIC-ELEMENTS (x)
  (cond ((NULL x) 0)
         ((numberp (first x)) (+ (first x) (SUM-NUMERIC-ELEMENTS (rest x))))
	 (T (SUM-NUMERIC-ELEMENTS (rest x)))))
