(defun COMBINE (x y)
  (+ x y))

(defun FIB (x)
  (cond ((or (equal 0 x) (equal 1 x)) 1)
	(+ x (+ (FIB (- x 1)) (FIB (- x 2))))))
