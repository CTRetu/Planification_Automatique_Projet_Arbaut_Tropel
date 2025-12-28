(define (problem simple-move-problem)
  (:domain simple-move)

  (:objects
    A B C
  )

  (:init
    (at A)
    (connected A B)
    (connected B C)
  )

  (:goal
    (at C)
  )
)
