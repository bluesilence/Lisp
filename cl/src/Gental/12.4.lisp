(defstruct node
  (name NIL)
  (question NIL)
  (yes-case NIL)
  (no-case NIL))

(setf *node-list* NIL)

(defun init ()
  (setf *node-list* NIL))

(defun add-node (name question yes-case no-case)
  (push *node-list* (make-node name question yes-case no-case))
  name)
