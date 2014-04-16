(defun COUNT-ATOMS (x)
  (cond ((NULL x) 0)
	((ATOM x) 1)
	(T (+ (COUNT-ATOMS (CAR x)) (COUNT-ATOMS (CDR x))))))
