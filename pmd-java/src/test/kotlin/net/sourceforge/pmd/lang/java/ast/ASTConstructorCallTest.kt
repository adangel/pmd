package net.sourceforge.pmd.lang.java.ast

import net.sourceforge.pmd.lang.ast.test.shouldBe

class ASTConstructorCallTest : ParserTestSpec({

    parserTest("Class instance creation") {

        "new Foo(a)" should matchExpr<ASTConstructorCall> {

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"
            }

            it::getArguments shouldBe child {

                child<ASTAmbiguousNameExpr> { }
            }
        }

        "new <Bar> Foo<F>()" should matchExpr<ASTConstructorCall> {

            it::getExplicitTypeArguments shouldBePresent child {
                unspecifiedChild()
            }

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"

                it::getTypeArguments shouldBePresent child {
                    unspecifiedChild()
                }
            }

            it::getArguments shouldBe child {}
        }

        "new @Lol Foo<F>()" should matchExpr<ASTConstructorCall> {

            it::getExplicitTypeArguments.shouldBeEmpty()

            child<ASTAnnotation>(ignoreChildren = true) {}

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"

                it::getTypeArguments shouldBePresent child {
                    unspecifiedChild()
                }
            }

            it::getArguments shouldBe child {}
        }
    }


    parserTest("Qualified class instance creation") {

        "a.g.c.new Foo(a)" should matchExpr<ASTConstructorCall> {

            it::getLhsExpression shouldBePresent child<ASTAmbiguousNameExpr> {
                it::getImage shouldBe "a.g.c"
            }

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"
            }

            it::getArguments shouldBe child {

                child<ASTAmbiguousNameExpr> { }
            }
        }

        "new O().new <Bar> Foo<F>()" should matchExpr<ASTConstructorCall> {

            it::getLhsExpression shouldBePresent child<ASTConstructorCall> {

                it::getTypeNode shouldBe child {
                    it::getTypeImage shouldBe "O"
                }

                it::getArguments shouldBe child {}
            }

            it::getExplicitTypeArguments shouldBePresent child {
                unspecifiedChild()
            }

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"

                it::getTypeArguments shouldBePresent child {
                    unspecifiedChild()
                }
            }

            it::getArguments shouldBe child {}
        }

        "method().new @Lol Foo<F>()" should matchExpr<ASTConstructorCall> {

            it::getLhsExpression shouldBePresent child<ASTMethodCall> {
                it::getMethodName shouldBe "method"
                it::getArguments shouldBe child {}
            }

            it::getExplicitTypeArguments.shouldBeEmpty()

            child<ASTAnnotation>(ignoreChildren = true) {}

            it::getTypeNode shouldBe child {
                it::getTypeImage shouldBe "Foo"

                it::getTypeArguments shouldBePresent child {
                    unspecifiedChild()
                }
            }

            it::getArguments shouldBe child {}
        }
    }

    parserTest("Array creation") {

        "new int[2][]" should matchExpr<ASTArrayAllocation> {

            it::getElementTypeNode shouldBe child<ASTPrimitiveType> {
                it::getModelConstant shouldBe ASTPrimitiveType.PrimitiveType.INT
                it::getTypeImage shouldBe "int"
            }

            it::getArrayDims shouldBe child {
                it::getArrayDepth shouldBe 2

                child<ASTNumericLiteral> {}
            }
        }

        "new @Foo int[3][2]" should matchExpr<ASTArrayAllocation> {
            child<ASTAnnotation> {
                it::getAnnotationName shouldBe "Foo"

                child<ASTMarkerAnnotation> {
                    it::getAnnotationName shouldBe "Foo"

                    child<ASTName> {
                        it::getNameDeclaration shouldBe null
                    }
                }
            }

            it::getElementTypeNode shouldBe child<ASTPrimitiveType> {
                it::isBoolean shouldBe false
                it::getModelConstant shouldBe ASTPrimitiveType.PrimitiveType.INT
                it::getTypeImage shouldBe "int"
            }

            it::getArrayDims shouldBe child {
                it::isArray shouldBe true
                it::getArrayDepth shouldBe 2

                child<ASTNumericLiteral> {
                    it::getValueAsInt shouldBe 3
                }
                child<ASTNumericLiteral> {
                    it::getValueAsInt shouldBe 2
                }
            }
        }

        "(new int[3])[2]" should matchExpr<ASTArrayAccess> {
            child<ASTParenthesizedExpression> {

                it::getWrappedExpression shouldBe child<ASTArrayAllocation> {

                    it::getElementTypeNode shouldBe child<ASTPrimitiveType> {
                        it::getTypeImage shouldBe "int"
                    }

                    it::getArrayDims shouldBe child {
                        it::isArray shouldBe true
                        it::getArrayDepth shouldBe 1

                        child<ASTNumericLiteral> {}
                    }
                }
            }
            child<ASTNumericLiteral> {}
        }

        "new Foo[0]" should matchExpr<ASTArrayAllocation> {
            it::getElementTypeNode shouldBe child<ASTClassOrInterfaceType> {
                it::isAnonymousClass shouldBe false
                it::isReferenceToClassSameCompilationUnit shouldBe true
                it::getTypeImage shouldBe "Foo"
            }

            it::getArrayDims shouldBe child {
                it::isArray shouldBe true
                it::getArrayDepth shouldBe 1

                child<ASTNumericLiteral> {
                    it::getValueAsInt shouldBe 0
                }
            }
        }


        "new Foo[] { f, g }" should matchExpr<ASTArrayAllocation> {

            it::getElementTypeNode shouldBe child<ASTClassOrInterfaceType> {
                it::isAnonymousClass shouldBe false
                it::isReferenceToClassSameCompilationUnit shouldBe true
                it::getTypeImage shouldBe "Foo"
            }

            it::getArrayDims shouldBe child {
                it::isArray shouldBe true
                it::getArrayDepth shouldBe 1

                child<ASTArrayInitializer> {
                    child<ASTVariableInitializer> {
                        child<ASTAmbiguousNameExpr> {}
                    }
                    child<ASTVariableInitializer> {
                        child<ASTAmbiguousNameExpr> {}
                    }
                }
            }
        }

        "new int[][] { { 1 }, { 2 } }" should matchExpr<ASTArrayAllocation> {

            it::getElementTypeNode shouldBe child<ASTPrimitiveType> {
                it::isBoolean shouldBe false
                it::getModelConstant shouldBe ASTPrimitiveType.PrimitiveType.INT
                it::getTypeImage shouldBe "int"
            }

            it::getArrayDims shouldBe child {
                it::isArray shouldBe true
                it::getArrayDepth shouldBe 2

                child<ASTArrayInitializer> {
                    child<ASTVariableInitializer> {
                        child<ASTArrayInitializer> {
                            child<ASTVariableInitializer> {
                                child<ASTNumericLiteral> {
                                    it::getValueAsInt shouldBe 1
                                }
                            }
                        }
                    }
                    child<ASTVariableInitializer> {
                        child<ASTArrayInitializer> {
                            child<ASTVariableInitializer> {
                                child<ASTNumericLiteral> {
                                    it::getValueAsInt shouldBe 2
                                }
                            }
                        }
                    }
                }
            }
        }

        "new int[][] { { 1 , 2 }, null }" should matchExpr<ASTArrayAllocation> {

            it::getElementTypeNode shouldBe child<ASTPrimitiveType> {
                it::getTypeImage shouldBe "int"
            }

            it::getArrayDims shouldBe child {
                it::isArray shouldBe true
                it::getArrayDepth shouldBe 2

                child<ASTArrayInitializer> {
                    child<ASTVariableInitializer> {
                        child<ASTArrayInitializer> {
                            child<ASTVariableInitializer> {
                                child<ASTNumericLiteral> {
                                    it::getValueAsInt shouldBe 1
                                }
                            }
                            child<ASTVariableInitializer> {
                                child<ASTNumericLiteral> {
                                    it::getValueAsInt shouldBe 2
                                }
                            }
                        }
                    }
                    child<ASTVariableInitializer> {
                        child<ASTNullLiteral> {}
                    }
                }
            }
        }
    }
})