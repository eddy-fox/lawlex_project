# -*- coding: utf-8 -*-
"""
Created on Mon Nov  3 14:41:40 2025

@author: soldesk
"""

import os
import sys
import cv2
import easyocr

reader = easyocr.Reader(['ko', 'en'])

# 이미지 경로 입력
if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.stderr.write("ERROR: missing args. usage: python ocr.py <image_path>\n")
        sys.exit(2)

    img_path = sys.argv[1]

    # 이미지 읽기
    img = cv2.imread(img_path)
    if img is None:
        raise ValueError("이미지를 읽지 못했습니다. 경로나 파일 손상 여부를 확인하세요.")
        
    result = reader.readtext(img)

    # 결과 출력
    texts = [detection[1] for detection in result]
    print(texts) 
