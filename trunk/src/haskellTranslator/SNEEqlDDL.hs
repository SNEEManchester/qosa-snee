module SNEEqlDDL 
where

import Char
import SNEEqlDDLLexer
import SNEEqlDDLAst
import SNEEqlAst
import SNEEqlEnvironment

-- parser produced by Happy Version 1.17

data HappyAbsSyn t4 t5 t6 t7 t8 t9 t10 t11 t12 t13 t14 t15 t16 t17 t18 t19
	= HappyTerminal DDLItem
	| HappyErrorToken Int
	| HappyAbsSyn4 t4
	| HappyAbsSyn5 t5
	| HappyAbsSyn6 t6
	| HappyAbsSyn7 t7
	| HappyAbsSyn8 t8
	| HappyAbsSyn9 t9
	| HappyAbsSyn10 t10
	| HappyAbsSyn11 t11
	| HappyAbsSyn12 t12
	| HappyAbsSyn13 t13
	| HappyAbsSyn14 t14
	| HappyAbsSyn15 t15
	| HappyAbsSyn16 t16
	| HappyAbsSyn17 t17
	| HappyAbsSyn18 t18
	| HappyAbsSyn19 t19

action_0 (37) = happyShift action_3
action_0 (4) = happyGoto action_4
action_0 (6) = happyGoto action_5
action_0 _ = happyFail

action_1 (37) = happyShift action_3
action_1 (6) = happyGoto action_2
action_1 _ = happyFail

action_2 (37) = happyShift action_7
action_2 (7) = happyGoto action_9
action_2 _ = happyFail

action_3 (38) = happyShift action_8
action_3 _ = happyFail

action_4 (39) = happyAccept
action_4 _ = happyFail

action_5 (37) = happyShift action_7
action_5 (7) = happyGoto action_6
action_5 _ = happyFail

action_6 (37) = happyShift action_17
action_6 (5) = happyGoto action_10
action_6 (9) = happyGoto action_14
action_6 (10) = happyGoto action_15
action_6 (11) = happyGoto action_16
action_6 _ = happyFail

action_7 (26) = happyShift action_13
action_7 _ = happyFail

action_8 (31) = happyShift action_12
action_8 _ = happyFail

action_9 (37) = happyShift action_11
action_9 (5) = happyGoto action_10
action_9 _ = happyFail

action_10 (37) = happyShift action_30
action_10 (9) = happyGoto action_29
action_10 (10) = happyGoto action_15
action_10 (11) = happyGoto action_16
action_10 _ = happyFail

action_11 (27) = happyShift action_18
action_11 _ = happyFail

action_12 (32) = happyShift action_28
action_12 _ = happyFail

action_13 (36) = happyShift action_27
action_13 _ = happyFail

action_14 (37) = happyShift action_26
action_14 (8) = happyGoto action_24
action_14 (10) = happyGoto action_25
action_14 (11) = happyGoto action_16
action_14 _ = happyFail

action_15 _ = happyReduce_7

action_16 (37) = happyShift action_23
action_16 (13) = happyGoto action_20
action_16 (14) = happyGoto action_21
action_16 (15) = happyGoto action_22
action_16 _ = happyFail

action_17 (27) = happyShift action_18
action_17 (29) = happyShift action_19
action_17 _ = happyFail

action_18 (36) = happyShift action_41
action_18 _ = happyFail

action_19 (25) = happyShift action_40
action_19 _ = happyFail

action_20 (37) = happyShift action_39
action_20 (14) = happyGoto action_37
action_20 (15) = happyGoto action_22
action_20 (18) = happyGoto action_38
action_20 _ = happyFail

action_21 _ = happyReduce_12

action_22 (37) = happyShift action_36
action_22 (17) = happyGoto action_35
action_22 _ = happyFail

action_23 (23) = happyShift action_34
action_23 _ = happyFail

action_24 _ = happyReduce_2

action_25 _ = happyReduce_8

action_26 (29) = happyShift action_19
action_26 (34) = happyShift action_33
action_26 _ = happyFail

action_27 _ = happyReduce_5

action_28 (35) = happyShift action_32
action_28 _ = happyFail

action_29 (37) = happyShift action_26
action_29 (8) = happyGoto action_31
action_29 (10) = happyGoto action_25
action_29 (11) = happyGoto action_16
action_29 _ = happyFail

action_30 (29) = happyShift action_19
action_30 _ = happyFail

action_31 _ = happyReduce_1

action_32 (21) = happyShift action_53
action_32 _ = happyFail

action_33 (26) = happyShift action_52
action_33 _ = happyFail

action_34 (25) = happyShift action_51
action_34 _ = happyFail

action_35 (37) = happyShift action_50
action_35 (16) = happyGoto action_49
action_35 _ = happyFail

action_36 (30) = happyShift action_48
action_36 _ = happyFail

action_37 _ = happyReduce_13

action_38 (37) = happyShift action_47
action_38 (12) = happyGoto action_46
action_38 _ = happyFail

action_39 (23) = happyShift action_34
action_39 (28) = happyShift action_45
action_39 _ = happyFail

action_40 (35) = happyShift action_44
action_40 _ = happyFail

action_41 (20) = happyShift action_43
action_41 (19) = happyGoto action_42
action_41 _ = happyFail

action_42 (37) = happyShift action_63
action_42 _ = happyFail

action_43 (33) = happyShift action_62
action_43 _ = happyReduce_19

action_44 (21) = happyShift action_61
action_44 _ = happyFail

action_45 (36) = happyShift action_60
action_45 _ = happyFail

action_46 _ = happyReduce_9

action_47 (34) = happyShift action_59
action_47 _ = happyFail

action_48 (22) = happyShift action_58
action_48 _ = happyFail

action_49 _ = happyReduce_14

action_50 (34) = happyShift action_57
action_50 _ = happyFail

action_51 (35) = happyShift action_56
action_51 _ = happyFail

action_52 (36) = happyShift action_55
action_52 _ = happyFail

action_53 (24) = happyShift action_54
action_53 _ = happyFail

action_54 (35) = happyShift action_72
action_54 _ = happyFail

action_55 _ = happyReduce_6

action_56 (21) = happyShift action_71
action_56 _ = happyFail

action_57 (23) = happyShift action_70
action_57 _ = happyFail

action_58 (35) = happyShift action_69
action_58 _ = happyFail

action_59 (29) = happyShift action_68
action_59 _ = happyFail

action_60 (20) = happyShift action_43
action_60 (19) = happyGoto action_67
action_60 _ = happyFail

action_61 (36) = happyShift action_66
action_61 _ = happyFail

action_62 (20) = happyShift action_43
action_62 (19) = happyGoto action_65
action_62 _ = happyFail

action_63 (34) = happyShift action_64
action_63 _ = happyFail

action_64 (27) = happyShift action_79
action_64 _ = happyFail

action_65 _ = happyReduce_20

action_66 _ = happyReduce_10

action_67 (37) = happyShift action_78
action_67 _ = happyFail

action_68 (36) = happyShift action_77
action_68 _ = happyFail

action_69 (21) = happyShift action_76
action_69 _ = happyFail

action_70 (36) = happyShift action_75
action_70 _ = happyFail

action_71 (36) = happyShift action_74
action_71 _ = happyFail

action_72 (21) = happyShift action_73
action_72 _ = happyFail

action_73 (38) = happyShift action_83
action_73 _ = happyFail

action_74 _ = happyReduce_15

action_75 _ = happyReduce_16

action_76 (34) = happyShift action_82
action_76 _ = happyFail

action_77 _ = happyReduce_11

action_78 (34) = happyShift action_81
action_78 _ = happyFail

action_79 (36) = happyShift action_80
action_79 _ = happyFail

action_80 _ = happyReduce_3

action_81 (28) = happyShift action_86
action_81 _ = happyFail

action_82 (36) = happyShift action_85
action_82 _ = happyFail

action_83 (36) = happyShift action_84
action_83 _ = happyFail

action_84 _ = happyReduce_4

action_85 _ = happyReduce_17

action_86 (36) = happyShift action_87
action_86 _ = happyFail

action_87 _ = happyReduce_18

happyReduce_1 = happyReduce 5 4 happyReduction_1
happyReduction_1 (_ `HappyStk`
	(HappyAbsSyn9  happy_var_4) `HappyStk`
	(HappyAbsSyn5  happy_var_3) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn4
		 (DDL happy_var_3 happy_var_4
	) `HappyStk` happyRest

happyReduce_2 = happyReduce 4 4 happyReduction_2
happyReduction_2 (_ `HappyStk`
	(HappyAbsSyn9  happy_var_3) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn4
		 (DDL [] happy_var_3
	) `HappyStk` happyRest

happyReduce_3 = happyReduce 8 5 happyReduction_3
happyReduction_3 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	(HappyAbsSyn19  happy_var_4) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn5
		 (happy_var_4
	) `HappyStk` happyRest

happyReduce_4 = happyReduce 11 6 happyReduction_4
happyReduction_4 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn6
		 (
	) `HappyStk` happyRest

happyReduce_5 = happySpecReduce_3  7 happyReduction_5
happyReduction_5 _
	_
	_
	 =  HappyAbsSyn7
		 (
	)

happyReduce_6 = happyReduce 4 8 happyReduction_6
happyReduction_6 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn8
		 (
	) `HappyStk` happyRest

happyReduce_7 = happySpecReduce_1  9 happyReduction_7
happyReduction_7 (HappyAbsSyn10  happy_var_1)
	 =  HappyAbsSyn9
		 ([happy_var_1]
	)
happyReduction_7 _  = notHappyAtAll 

happyReduce_8 = happySpecReduce_2  9 happyReduction_8
happyReduction_8 (HappyAbsSyn10  happy_var_2)
	(HappyAbsSyn9  happy_var_1)
	 =  HappyAbsSyn9
		 (happy_var_2 : happy_var_1
	)
happyReduction_8 _ _  = notHappyAtAll 

happyReduce_9 = happyReduce 4 10 happyReduction_9
happyReduction_9 (_ `HappyStk`
	(HappyAbsSyn18  happy_var_3) `HappyStk`
	(HappyAbsSyn13  happy_var_2) `HappyStk`
	(HappyAbsSyn11  happy_var_1) `HappyStk`
	happyRest)
	 = HappyAbsSyn10
		 (Stream happy_var_1 happy_var_2 happy_var_3
	) `HappyStk` happyRest

happyReduce_10 = happyReduce 6 11 happyReduction_10
happyReduction_10 (_ `HappyStk`
	(HappyTerminal (DDLString happy_var_5)) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn11
		 (happy_var_5
	) `HappyStk` happyRest

happyReduce_11 = happyReduce 4 12 happyReduction_11
happyReduction_11 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn12
		 (
	) `HappyStk` happyRest

happyReduce_12 = happySpecReduce_1  13 happyReduction_12
happyReduction_12 (HappyAbsSyn14  happy_var_1)
	 =  HappyAbsSyn13
		 ([happy_var_1]
	)
happyReduction_12 _  = notHappyAtAll 

happyReduce_13 = happySpecReduce_2  13 happyReduction_13
happyReduction_13 (HappyAbsSyn14  happy_var_2)
	(HappyAbsSyn13  happy_var_1)
	 =  HappyAbsSyn13
		 (happy_var_2 : happy_var_1
	)
happyReduction_13 _ _  = notHappyAtAll 

happyReduce_14 = happySpecReduce_3  14 happyReduction_14
happyReduction_14 _
	(HappyAbsSyn17  happy_var_2)
	(HappyAbsSyn15  happy_var_1)
	 =  HappyAbsSyn14
		 (DeclAtt (AttributeName happy_var_1) happy_var_2
	)
happyReduction_14 _ _ _  = notHappyAtAll 

happyReduce_15 = happyReduce 6 15 happyReduction_15
happyReduction_15 (_ `HappyStk`
	(HappyTerminal (DDLString happy_var_5)) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn15
		 (happy_var_5
	) `HappyStk` happyRest

happyReduce_16 = happyReduce 4 16 happyReduction_16
happyReduction_16 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn16
		 (
	) `HappyStk` happyRest

happyReduce_17 = happyReduce 7 17 happyReduction_17
happyReduction_17 (_ `HappyStk`
	_ `HappyStk`
	(HappyTerminal (DDLString happy_var_5)) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn17
		 (toTypeToken happy_var_5
	) `HappyStk` happyRest

happyReduce_18 = happyReduce 8 18 happyReduction_18
happyReduction_18 (_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	(HappyAbsSyn19  happy_var_4) `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	_ `HappyStk`
	happyRest)
	 = HappyAbsSyn18
		 (happy_var_4
	) `HappyStk` happyRest

happyReduce_19 = happySpecReduce_1  19 happyReduction_19
happyReduction_19 (HappyTerminal (DDLInt happy_var_1))
	 =  HappyAbsSyn19
		 ([happy_var_1]
	)
happyReduction_19 _  = notHappyAtAll 

happyReduce_20 = happySpecReduce_3  19 happyReduction_20
happyReduction_20 (HappyAbsSyn19  happy_var_3)
	_
	(HappyTerminal (DDLInt happy_var_1))
	 =  HappyAbsSyn19
		 (happy_var_1 : happy_var_3
	)
happyReduction_20 _ _ _  = notHappyAtAll 

happyNewToken action sts stk [] =
	action 39 39 notHappyAtAll (HappyState action) sts stk []

happyNewToken action sts stk (tk:tks) =
	let cont i = action i i tk (HappyState action) sts stk tks in
	case tk of {
	DDLInt happy_dollar_dollar -> cont 20;
	DDLString happy_dollar_dollar -> cont 21;
	DDLClass -> cont 22;
	DDLColumn -> cont 23;
	DDLEncoding -> cont 24;
	DDLName -> cont 25;
	DDLSchema -> cont 26;
	DDLSinks -> cont 27;
	DDLSites -> cont 28;
	DDLStream -> cont 29;
	DDLType -> cont 30;
	DDLXml -> cont 31;
	DDLVersion -> cont 32;
	DDLComma -> cont 33;
	DDLBackSlash -> cont 34;
	DDLEquals -> cont 35;
	DDLGreaterThan -> cont 36;
	DDLLessThan -> cont 37;
	DDLQuestionMark -> cont 38;
	_ -> happyError' (tk:tks)
	}

happyError_ tk tks = happyError' (tk:tks)

newtype HappyIdentity a = HappyIdentity a
happyIdentity = HappyIdentity
happyRunIdentity (HappyIdentity a) = a

instance Monad HappyIdentity where
    return = HappyIdentity
    (HappyIdentity p) >>= q = q p

happyThen :: () => HappyIdentity a -> (a -> HappyIdentity b) -> HappyIdentity b
happyThen = (>>=)
happyReturn :: () => a -> HappyIdentity a
happyReturn = (return)
happyThen1 m k tks = (>>=) m (\a -> k a tks)
happyReturn1 :: () => a -> b -> HappyIdentity a
happyReturn1 = \a tks -> (return) a
happyError' :: () => [DDLItem] -> HappyIdentity a
happyError' = HappyIdentity . ddlParseError

ddlParse tks = happyRunIdentity happySomeParser where
  happySomeParser = happyThen (happyParse action_0 tks) (\x -> case x of {HappyAbsSyn4 z -> happyReturn z; _other -> notHappyAtAll })

happySeq = happyDontSeq


ddlParseError :: [DDLItem] -> a
ddlParseError _ = error "DDL Parse error"
{-# LINE 1 "templates/GenericTemplate.hs" #-}
{-# LINE 1 "templates/GenericTemplate.hs" #-}
{-# LINE 1 "<built-in>" #-}
{-# LINE 1 "<command line>" #-}
{-# LINE 1 "templates/GenericTemplate.hs" #-}
-- Id: GenericTemplate.hs,v 1.26 2005/01/14 14:47:22 simonmar Exp 

{-# LINE 28 "templates/GenericTemplate.hs" #-}








{-# LINE 49 "templates/GenericTemplate.hs" #-}

{-# LINE 59 "templates/GenericTemplate.hs" #-}

{-# LINE 68 "templates/GenericTemplate.hs" #-}

infixr 9 `HappyStk`
data HappyStk a = HappyStk a (HappyStk a)

-----------------------------------------------------------------------------
-- starting the parse

happyParse start_state = happyNewToken start_state notHappyAtAll notHappyAtAll

-----------------------------------------------------------------------------
-- Accepting the parse

-- If the current token is (1), it means we've just accepted a partial
-- parse (a %partial parser).  We must ignore the saved token on the top of
-- the stack in this case.
happyAccept (1) tk st sts (_ `HappyStk` ans `HappyStk` _) =
	happyReturn1 ans
happyAccept j tk st sts (HappyStk ans _) = 
	 (happyReturn1 ans)

-----------------------------------------------------------------------------
-- Arrays only: do the next action

{-# LINE 155 "templates/GenericTemplate.hs" #-}

-----------------------------------------------------------------------------
-- HappyState data type (not arrays)



newtype HappyState b c = HappyState
        (Int ->                    -- token number
         Int ->                    -- token number (yes, again)
         b ->                           -- token semantic value
         HappyState b c ->              -- current state
         [HappyState b c] ->            -- state stack
         c)



-----------------------------------------------------------------------------
-- Shifting a token

happyShift new_state (1) tk st sts stk@(x `HappyStk` _) =
     let i = (case x of { HappyErrorToken (i) -> i }) in
--     trace "shifting the error token" $
     new_state i i tk (HappyState (new_state)) ((st):(sts)) (stk)

happyShift new_state i tk st sts stk =
     happyNewToken new_state ((st):(sts)) ((HappyTerminal (tk))`HappyStk`stk)

-- happyReduce is specialised for the common cases.

happySpecReduce_0 i fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happySpecReduce_0 nt fn j tk st@((HappyState (action))) sts stk
     = action nt j tk st ((st):(sts)) (fn `HappyStk` stk)

happySpecReduce_1 i fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happySpecReduce_1 nt fn j tk _ sts@(((st@(HappyState (action))):(_))) (v1`HappyStk`stk')
     = let r = fn v1 in
       happySeq r (action nt j tk st sts (r `HappyStk` stk'))

happySpecReduce_2 i fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happySpecReduce_2 nt fn j tk _ ((_):(sts@(((st@(HappyState (action))):(_))))) (v1`HappyStk`v2`HappyStk`stk')
     = let r = fn v1 v2 in
       happySeq r (action nt j tk st sts (r `HappyStk` stk'))

happySpecReduce_3 i fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happySpecReduce_3 nt fn j tk _ ((_):(((_):(sts@(((st@(HappyState (action))):(_))))))) (v1`HappyStk`v2`HappyStk`v3`HappyStk`stk')
     = let r = fn v1 v2 v3 in
       happySeq r (action nt j tk st sts (r `HappyStk` stk'))

happyReduce k i fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happyReduce k nt fn j tk st sts stk
     = case happyDrop (k - ((1) :: Int)) sts of
	 sts1@(((st1@(HappyState (action))):(_))) ->
        	let r = fn stk in  -- it doesn't hurt to always seq here...
       		happyDoSeq r (action nt j tk st1 sts1 r)

happyMonadReduce k nt fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happyMonadReduce k nt fn j tk st sts stk =
        happyThen1 (fn stk tk) (\r -> action nt j tk st1 sts1 (r `HappyStk` drop_stk))
       where sts1@(((st1@(HappyState (action))):(_))) = happyDrop k ((st):(sts))
             drop_stk = happyDropStk k stk

happyMonad2Reduce k nt fn (1) tk st sts stk
     = happyFail (1) tk st sts stk
happyMonad2Reduce k nt fn j tk st sts stk =
       happyThen1 (fn stk tk) (\r -> happyNewToken new_state sts1 (r `HappyStk` drop_stk))
       where sts1@(((st1@(HappyState (action))):(_))) = happyDrop k ((st):(sts))
             drop_stk = happyDropStk k stk





             new_state = action


happyDrop (0) l = l
happyDrop n ((_):(t)) = happyDrop (n - ((1) :: Int)) t

happyDropStk (0) l = l
happyDropStk n (x `HappyStk` xs) = happyDropStk (n - ((1)::Int)) xs

-----------------------------------------------------------------------------
-- Moving to a new state after a reduction

{-# LINE 253 "templates/GenericTemplate.hs" #-}
happyGoto action j tk st = action j j tk (HappyState action)


-----------------------------------------------------------------------------
-- Error recovery ((1) is the error token)

-- parse error if we are in recovery and we fail again
happyFail  (1) tk old_st _ stk =
--	trace "failing" $ 
    	happyError_ tk

{-  We don't need state discarding for our restricted implementation of
    "error".  In fact, it can cause some bogus parses, so I've disabled it
    for now --SDM

-- discard a state
happyFail  (1) tk old_st (((HappyState (action))):(sts)) 
						(saved_tok `HappyStk` _ `HappyStk` stk) =
--	trace ("discarding state, depth " ++ show (length stk))  $
	action (1) (1) tk (HappyState (action)) sts ((saved_tok`HappyStk`stk))
-}

-- Enter error recovery: generate an error token,
--                       save the old token and carry on.
happyFail  i tk (HappyState (action)) sts stk =
--      trace "entering error recovery" $
	action (1) (1) tk (HappyState (action)) sts ( (HappyErrorToken (i)) `HappyStk` stk)

-- Internal happy errors:

notHappyAtAll = error "Internal Happy error\n"

-----------------------------------------------------------------------------
-- Hack to get the typechecker to accept our action functions







-----------------------------------------------------------------------------
-- Seq-ing.  If the --strict flag is given, then Happy emits 
--	happySeq = happyDoSeq
-- otherwise it emits
-- 	happySeq = happyDontSeq

happyDoSeq, happyDontSeq :: a -> b -> b
happyDoSeq   a b = a `seq` b
happyDontSeq a b = b

-----------------------------------------------------------------------------
-- Don't inline any functions from the template.  GHC has a nasty habit
-- of deciding to inline happyGoto everywhere, which increases the size of
-- the generated parser quite a bit.

{-# LINE 317 "templates/GenericTemplate.hs" #-}
{-# NOINLINE happyShift #-}
{-# NOINLINE happySpecReduce_0 #-}
{-# NOINLINE happySpecReduce_1 #-}
{-# NOINLINE happySpecReduce_2 #-}
{-# NOINLINE happySpecReduce_3 #-}
{-# NOINLINE happyReduce #-}
{-# NOINLINE happyMonadReduce #-}
{-# NOINLINE happyGoto #-}
{-# NOINLINE happyFail #-}

-- end of Happy Template.
