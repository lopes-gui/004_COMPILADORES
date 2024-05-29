package alguma;

import com.google.inject.Inject
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder

class Alguma2JvmModelInferrer extends AbstractModelInferrer {
	
	@Inject extension JvmTypesBuilder
  	
	def dispatch void infer(Programa element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass("Programa2")) [
 			for (declaracao : element.declaracoes) {
 				if(declaracao.tipo == "INTEIRO")
	 				members += declaracao.toField(declaracao.nome, typeRef(int)) [ static = true ]
	 			else 
		 			members += declaracao.toField(declaracao.nome, typeRef(double)) [ static = true ]
 			}
 			members += element.toMethod("main", typeRef(void)) [
 				static = true
 				parameters.add(element.toParameter("args",typeRef(String).addArrayTypeDimension))
				body = element.algoritmo
 			]
		]
	}
}