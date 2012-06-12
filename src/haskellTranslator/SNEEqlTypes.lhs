\begin{comment} 
The material in the code sections is the actual live copy of the working Haskell including all the latest changes.
\begin{code}
module SNEEqlTypes
where
\end{code}
\end{comment}

\subsection{Data Types}
\SNEEql supports the primitive types \typ{int}, \typ{float}, \typ{string} and \typ{Boolean}.
All individual attributes will type to one of the primitive types.

\SNEEql uses the collection types bags, tuples and streams. Represented as \bagof{$\tau$},
\tupleof{$\tau_1$, \mdots , $\tau_n$}
and \streamof{$\tau$} respectively.
Where $\tau$ is used to represent a type variable.

In Haskell the types are represented as:
\begin{code}
data Type
           -- Primitive Types
           = Boolean
           | Int
           | Float
           | String
           -- Collection Types
           | Streamof Type           
           | Tupleof [Type]
           | Bagof Type
\end{code}           
\begin{comment}
\begin{code}
           -- Test variable
           | NullType
            deriving (Eq, Ord, Show, Read)
\end{code}
\end{comment}    

Similar to standard query languages \SNEEql supports Relations which are bags of tuples represented as:

\bagof{\tupleof{$\tau_1$, \mdots, $\tau_n$}}.
\newline

\SNEEql also supports unbounded streams of tuples.
Because both the order and insertion of tuples is important for some operators, \SNEEql uses the concept of a tagged tuple which includes two extra int values representing the tuples position in the stream and the its insertion time.
A stream of these tagged tuples is represented as: 

\streamof{\tupleof{\typ{int}, \typ{int}, \tupleof{$\tau_1$, \mdots, $\tau_n$}}}
\newline

A subsection of an unbounded stream of tuples can be collected together to form a window which is bounded bag of tuples much like a normal relation. Because the creation time of windows is important for some operators, each window is tagged with an extra int value representing its creation time.
A window is represented as:

\tupleof{\typ{int}, \bagof{\tupleof{$\tau_1$, \mdots, $\tau_n$}}}
\newline

\SNEEql queries are continuous queries and therefore produce not one but a stream of windows which is represented as:

\streamof{\tupleof{\typ{int}, \bagof{\tupleof{$\tau_1$, \mdots, $\tau_n$}}}}

																				