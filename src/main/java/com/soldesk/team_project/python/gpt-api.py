# -*- coding: utf-8 -*-
"""
Created on Fri Sep 26 11:55:32 2025

@author: soldesk
"""
import os
import sys
import openai

# API 키 불러오기
def load_api_key():
    props_path = r"C:\secrets\lawlex_openApi.properties"
    if not os.path.exists(props_path):
        raise FileNotFoundError(f"API 키 파일을 찾을 수 없습니다: {props_path}")
    
    with open(props_path, "r", encoding="utf-8") as f:
        for line in f:
            if line.startswith("open.api-key"):
                return line.strip().split("=", 1)[1]
            
    raise ValueError("lawlex_openApi.properties에 open.api-key 항목이 없습니다.")

api_key = load_api_key()
client = openai.OpenAI(api_key = api_key)

# 정의
def return_answer(question_title, question_category, question_content):
    
    system_prompt = """사용자가 법률 자문을 구하려고 합니다.
    분야에 대한 대한민국의 법률 및 제목과 질문에 대한 판례를 바탕으로 답변하세요.
    사용자에게 추가적인 정보를 요구하거나 후속 질문에 대한 추천은 하지않고, 주어진 정보에 대한 답변만 해주세요.
    답변은 두 문단으로, 너무 길지 않게 해주세요."""

    user_question = "제목: " + question_title + "\n분야: " + question_category + "\n질문: " + question_content

    response = client.chat.completions.create(
        model="gpt-5-nano",
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_question}
        ]
    )

    answer = ""
    try:
        answer = response.choices[0].message.content
    except Exception:
        answer = str(response)
    return answer

# 질문 입력
if __name__ == "__main__":
    if len(sys.argv) < 4:
        sys.stderr.write("ERROR: missing args. usage: python gpt-api.py <title> <category> <content>\n")
        sys.exit(2)

    question_title = sys.argv[1]
    question_category = sys.argv[2]
    question_content = sys.argv[3]

    # 결과 출력
    result = return_answer(question_title, question_category, question_content)
    print(result)
    





### 테스트
# =============================================================================
# 질문 입력
# question_title = "비율이 몇대몇정도 나올까요"
# question_category = "교통사고"
# question_content = """주행 중에 아이가 갑자기 도로로 뛰어나와 사고가 났습니다. 
# 어린이 보호구역이나 횡단보도도 아니었고 신호도 없었습니다.
# 이런 경우에는 과실이 어떻게 되나요?"""
#
# 결과 출력
# result = return_answer(question_title, question_category, question_content)
# print(result)
# =============================================================================
# 요지는 이렇습니다. 대한민국의 불법행위 손해배상에서 과실은 비례책임(비율책임)으로 나누며, 구체적인 비율은 사실관계와 증거에 따라 법원이 판단합니다. 질문의 상황(어린이가 갑자기 도로로 뛰어나왔고, 어린이 보호구역·횡단보도도 아니며 신호도 없었다)에서는 운전자의 주의의무가 크게 문제될 수 있지만, 아이의 급작스러운 돌진이라는 점도 고려됩니다. 일반적으로는 운전자 쪽의 과실이 더 큰 편이지만, 정확한 비율은 사건마다 달라집니다.
# 
# 일반적으로 자주 보이는 비율의 범위
# - 운전자 60%~90%, 보행자(또는 보호자) 10%~40%
# - 상황에 따라선 운전자 70%~80%, 보행자 20%~30% 정도로 보는 경우가 많습니다.
# - 운전자의 속도 과다, 시야 불량, 제동 여력이 제한된 상황 등 주의의무를 크게 위반한 경우에는 운전자 쪽 비율이 더 올라갈 수 있습니다.
# - 반대로 아이가 도로로 뛰어나온 것이 예측 가능했고 충분히 피할 수 있었던 경우나 운전자가 충분히 경계하며 속도를 낮췄다면 운전자 비율이 낮아질 수 있습니다.
# 
# 참고로 이 판단은 법원(또는 보험사)의 구체적인 증거·사실관계 평가에 따라 달라지므로, 같은 상황이라도 결과는 달라질 수 있습니다. 또한 어린이의 갑작스러운 행위 자체가 다소 불가항력에 가깝더라도, 차량은 보행자에 대해 충분한 주의의무를 이행해야 한다는 점이 일반적으로 강조됩니다.
# =============================================================================
